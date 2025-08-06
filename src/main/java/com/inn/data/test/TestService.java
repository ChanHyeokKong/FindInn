package com.inn.data.test;

import com.inn.data.member.MemberDto;

public interface TestService {
    CharacterType calculateCharacterType(AnswerRequest answers, MemberDto member);
}