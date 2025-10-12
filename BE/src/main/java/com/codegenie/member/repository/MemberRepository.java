package com.codegenie.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codegenie.member.entity.MemberEntity;



public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

	Boolean existsByEmail(String email);
	
	Optional<MemberEntity> findByEmail(String email);
	
}
