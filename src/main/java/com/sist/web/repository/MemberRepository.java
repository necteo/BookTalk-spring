package com.sist.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sist.web.entity.AuthProvider;
import com.sist.web.entity.Member;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {	
    // 기본 조회: provider + providerId (이메일 없어도 동작)
    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);

    // 같은 이메일로 다른 소셜 가입한 경우 감지
    Optional<Member> findByEmailAndProviderNot(String email, AuthProvider provider);
}
