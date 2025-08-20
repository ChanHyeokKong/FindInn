

package com.inn.data.test;

import com.inn.data.member.MemberDto;

public interface TestService {
    /** AnswerRequest를 분석해 대표 trait/캐릭터/퍼센트까지 계산해 반환 */
    TestResultDto calculateCharacterType(AnswerRequest answers, MemberDto member);
}