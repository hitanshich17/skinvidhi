package com.skinvidhi.core.routine;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Quiz answers in, AM and PM routine out. Nothing is stored. */
@RestController
@RequestMapping("/api/v1/routines")
public class RoutineController {

    private final RoutineCatalog catalog;

    public RoutineController(RoutineCatalog catalog) {
        this.catalog = catalog;
    }

    @PostMapping
    public RoutineResponse build(@RequestBody QuizAnswers answers) {
        RoutinePlan plan = RoutineRules.plan(answers, catalog.load());
        return RoutineResponse.of(RoutineSelector.select(plan, answers.budgetCents()));
    }

    /** Unreadable JSON, an unknown answer, or a rule in QuizAnswers broken: 400 with the reason. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail badAnswers(HttpMessageNotReadableException e) {
        Throwable cause = e;
        while (cause.getCause() != null && !(cause instanceof IllegalArgumentException)) {
            cause = cause.getCause();
        }
        String reason = cause instanceof IllegalArgumentException ? cause.getMessage() : "unreadable quiz answers";
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, reason);
    }
}
