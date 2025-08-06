package com.inn.service;

import com.inn.data.member.MemberDto;
import com.inn.data.test.AnswerRequest;
import com.inn.data.test.CharacterType;

public interface TestService {
    CharacterType calculateCharacterType(AnswerRequest request, MemberDto member);
}