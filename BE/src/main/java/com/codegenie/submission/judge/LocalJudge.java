// src/main/java/com/codegenie/submission/judge/LocalJudge.java
package com.codegenie.submission.judge;

import com.codegenie.workbooktestcase.entity.QuizTestcase;
import lombok.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
public class LocalJudge {

    private static final long TIMEOUT_MS = 2000; // per test

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class JudgeReport {
        private String status;      // Accepted / Wrong Answer / Compilation Error / Time Limit Exceeded / Runtime Error
        private String compileLog;
        private String runLog;
        private Double totalTime;   // seconds
    }

    public JudgeReport judge(String language, String code, List<QuizTestcase> cases) {
        File workDir = null;
        try {
            workDir = Files.createTempDirectory("judge_").toFile();
            String lang = (language == null ? "" : language.trim().toLowerCase(Locale.ROOT));
            return switch (lang) {
                case "python" -> judgePython(code, cases, workDir);
                case "java"   -> judgeJava(code, cases, workDir);
                case "c++", "cpp" -> judgeCpp(code, cases, workDir);
                default -> JudgeReport.builder().status("Wrong Answer").runLog("지원하지 않는 언어").build();
            };
        } catch (IOException e) {
            return JudgeReport.builder().status("Runtime Error").runLog(e.getMessage()).build();
        } finally {
            deleteRecursively(workDir);
        }
    }

    /* ---------------- Python ---------------- */

    private JudgeReport judgePython(String code, List<QuizTestcase> cases, File dir) throws IOException {
        File src = new File(dir, "main.py");
        writeText(src, code);

        double totalMs = 0;
        for (QuizTestcase tc : cases) {
            ExecResult r = exec(dir, new String[]{"python", src.getName()}, tc.getInputText(),
                    env -> {
                        env.put("PYTHONIOENCODING", "UTF-8");
                        env.put("PYTHONUTF8", "1");
                    });
            if (r.timedOut) return JudgeReport.builder().status("Time Limit Exceeded").runLog("TLE").build();
            if (r.exitCode != 0) return JudgeReport.builder().status("Runtime Error").runLog(r.stderr).build();
            totalMs += r.elapsedMs;
            if (!equalsNormalized(r.stdout, tc.getExpectedOut())) {
                return JudgeReport.builder().status("Wrong Answer").runLog(diffMsg(r.stdout, tc.getExpectedOut())).build();
            }
        }
        return JudgeReport.builder().status("Accepted").totalTime(totalMs / 1000.0).build();
    }

    /* ---------------- Java ---------------- */

    private JudgeReport judgeJava(String code, List<QuizTestcase> cases, File dir) throws IOException {
        File src = new File(dir, "Main.java");
        writeText(src, code);

        // compile with utf-8
        ExecResult c = exec(dir, new String[]{"javac", "-encoding", "UTF-8", src.getName()}, null,
                env -> {}); // no special env for compile
        if (c.timedOut) return JudgeReport.builder().status("Time Limit Exceeded").compileLog("Compile TLE").build();
        if (c.exitCode != 0) return JudgeReport.builder().status("Compilation Error").compileLog(c.stderr).build();

        double totalMs = 0;
        for (QuizTestcase tc : cases) {
            ExecResult r = exec(dir, javaRunCmd(), tc.getInputText(),
                    env -> env.put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8")); // safety net
            if (r.timedOut) return JudgeReport.builder().status("Time Limit Exceeded").runLog("TLE").build();
            if (r.exitCode != 0) return JudgeReport.builder().status("Runtime Error").runLog(r.stderr).build();
            totalMs += r.elapsedMs;
            if (!equalsNormalized(r.stdout, tc.getExpectedOut())) {
                return JudgeReport.builder().status("Wrong Answer").runLog(diffMsg(r.stdout, tc.getExpectedOut())).build();
            }
        }
        return JudgeReport.builder().status("Accepted").totalTime(totalMs / 1000.0).build();
    }

    // ✅ stdout/stderr 인코딩까지 명시적으로 UTF-8 고정
    private String[] javaRunCmd() {
        return new String[]{
                "java",
                "-Dfile.encoding=UTF-8",
                "-Dsun.stdout.encoding=UTF-8",
                "-Dsun.stderr.encoding=UTF-8",
                "Main"
        };
    }

    /* ---------------- C++ ---------------- */

    private JudgeReport judgeCpp(String code, List<QuizTestcase> cases, File dir) throws IOException {
        File src = new File(dir, "main.cpp");
        writeText(src, code);

        String exe = isWindows() ? "main.exe" : "main";
        File bin = new File(dir, exe);

        ExecResult c = exec(dir, new String[]{"g++", "-O2", "-std=c++17", src.getName(), "-o", bin.getName()}, null,
                env -> {});
        if (c.timedOut) return JudgeReport.builder().status("Time Limit Exceeded").compileLog("Compile TLE").build();
        if (c.exitCode != 0) return JudgeReport.builder().status("Compilation Error").compileLog(c.stderr).build();

        double totalMs = 0;
        for (QuizTestcase tc : cases) {
            String execPath = isWindows() ? bin.getName() : "./" + bin.getName();
            ExecResult r = exec(dir, new String[]{execPath}, tc.getInputText(), env -> {});
            if (r.timedOut) return JudgeReport.builder().status("Time Limit Exceeded").runLog("TLE").build();
            if (r.exitCode != 0) return JudgeReport.builder().status("Runtime Error").runLog(r.stderr).build();
            totalMs += r.elapsedMs;
            if (!equalsNormalized(r.stdout, tc.getExpectedOut())) {
                return JudgeReport.builder().status("Wrong Answer").runLog(diffMsg(r.stdout, tc.getExpectedOut())).build();
            }
        }
        return JudgeReport.builder().status("Accepted").totalTime(totalMs / 1000.0).build();
    }

    /* ---------------- Exec helper ---------------- */

    @Data
    private static class ExecResult {
        int exitCode;
        boolean timedOut;
        String stdout;
        String stderr;
        long elapsedMs;
    }

    private interface EnvPatcher { void apply(Map<String, String> env); }

    private ExecResult exec(File dir, String[] cmd, String stdin, EnvPatcher patcher) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(dir);
        pb.redirectErrorStream(false);

        // 환경변수 패치(UTF-8 강제용)
        if (patcher != null) patcher.apply(pb.environment());

        Process p = pb.start();
        Instant start = Instant.now();

        if (stdin != null) {
            try (OutputStream os = p.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } else {
            try { p.getOutputStream().close(); } catch (IOException ignored) {}
        }

        boolean finished;
        try {
            finished = p.waitFor(TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finished = false;
        }

        ExecResult r = new ExecResult();
        r.elapsedMs = Duration.between(start, Instant.now()).toMillis();

        if (!finished) {
            p.destroyForcibly();
            r.timedOut = true;
            r.exitCode = -1;
            r.stdout = "";
            r.stderr = "TIMEOUT";
            return r;
        }

        r.exitCode = p.exitValue();
        r.stdout = readStream(p.getInputStream()); // UTF-8로 해석
        r.stderr = readStream(p.getErrorStream());
        return r;
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            return new String(bis.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void writeText(File f, String s) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            w.write(s == null ? "" : s);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /** 개행 통일 + 라인 단위 rtrim + 전체 trim */
    private static String normalizeText(String s) {
        if (s == null) return "";
        String t = s.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = t.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(rtrim(lines[i]));
            if (i < lines.length - 1) sb.append('\n');
        }
        return sb.toString().trim();
    }

    private static String rtrim(String x) {
        int i = x.length() - 1;
        while (i >= 0 && Character.isWhitespace(x.charAt(i))) i--;
        return x.substring(0, i + 1);
    }

    private static boolean equalsNormalized(String out, String exp) {
        return normalizeText(out).equals(normalizeText(exp));
    }

    private static String diffMsg(String out, String exp) {
        return "[expected]\n" + normalizeText(exp) + "\n[actual]\n" + normalizeText(out);
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            if (arr != null) for (File c : arr) deleteRecursively(c);
        }
        try { Files.deleteIfExists(file.toPath()); } catch (IOException ignored) {}
    }
}
