package com.inn.data.test;

import java.util.List;

import lombok.Data;

import lombok.Data;
import java.util.List;

@Data
public class AnswerRequest {
	 private String q1;
	    private String q2;
	    private String q3;
	    private String q4;
	    private String q5;
	    private String q6;

	    public List<String> getAllAnswers() {
	        return List.of(q1, q2, q3, q4, q5, q6);
	    }
	}