package com.codegenie.workbook.repository;

import com.codegenie.workbook.entity.Workbook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkbookRepository extends JpaRepository<Workbook, Long> { }
