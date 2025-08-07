package com.inn.data.test;

import java.util.Optional;

import com.inn.data.member.MemberDto;
import com.inn.data.test.AnswerRequest;
import com.inn.data.test.CharacterType;

public interface TestService {
	  Optional<CharacterType> calculateCharacterType(AnswerRequest answers, MemberDto member);
}