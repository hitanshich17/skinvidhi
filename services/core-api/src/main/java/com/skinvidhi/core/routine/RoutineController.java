package com.skinvidhi.core.routine;

import com.skinvidhi.core.climate.Climate;
import com.skinvidhi.core.climate.ClimateService;
import com.skinvidhi.core.feedback.QuizSessionRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quiz answers in, AM and PM routine out. With an X-Client-Id header (a random UUID from the browser), the
 * routine and the answers (without pregnancy and city) are stored for the ranking model.
 */
@RestController
@RequestMapping("/api/v1/routines")
public class RoutineController {

    private final RoutineCatalog catalog;
    private final ClimateService climateService;
    private final QuizSessionRepository sessions;

    public RoutineController(RoutineCatalog catalog, ClimateService climateService, QuizSessionRepository sessions) {
        this.catalog = catalog;
        this.climateService = climateService;
        this.sessions = sessions;
    }

    @PostMapping
    public RoutineResponse build(@RequestHeader(value = "X-Client-Id", required = false) UUID clientId,
                                 @RequestBody QuizAnswers answers) {
        Climate climate = climateService.climateFor(answers.city()).orElse(null);
        RoutinePlan plan = RoutineRules.plan(answers, catalog.load(), climate);
        Routine routine = RoutineSelector.select(plan, answers.budgetCents());
        Long sessionId = null;
        if (clientId != null) {
            Map<String, String> shown = new LinkedHashMap<>();
            routine.picks().forEach((step, pick) -> shown.put(step.name(), pick.product().id()));
            sessionId = sessions.save(clientId, answers, climate == null ? Set.of() : climate.signals(), shown);
        }
        return RoutineResponse.of(routine, climate, sessionId);
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
