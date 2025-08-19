package com.inn.data.test;

import java.util.Map;

public class TestResultDto {
    private final Map<String, Integer> percents;   // {activity=60, healing=20, ...}
    private final CharacterType winnerType;        // 최종 캐릭터

    public TestResultDto(Map<String, Integer> percents, CharacterType winnerType) {
        this.percents = percents;
        this.winnerType = winnerType;
    }

    public Map<String, Integer> getPercents() { return percents; }
    public CharacterType getWinnerType() { return winnerType; }
}