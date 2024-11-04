package com.shallwecode.backend.problem.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindMyProblemResDTO {

    private Long problemId;
    private String title;
    private int problemLevel;
    private boolean isSolved;

    public FindMyProblemResDTO(Long problemId, String title, Integer problemLevel, Integer isSolved) {
        this.problemId = problemId;
        this.title = title;
        this.problemLevel = problemLevel;
        this.isSolved = (isSolved != null && isSolved > 0);
    }
}