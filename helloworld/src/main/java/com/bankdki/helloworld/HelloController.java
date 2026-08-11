package com.bankdki.helloworld;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String hello() {
        return "Hello World! Daniel!";
    }

    @Value
    public static class Result {
        int left;
        int right;
        long answer;
    }

    @GetMapping("/calc")
    public Result calc(@RequestParam int left, @RequestParam int right) {
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("left", left)
                .addValue("right", right);

        return jdbcTemplate.queryForObject(
                "SELECT :left + :right AS answer",
                source,
                (rs, rowNum) -> new Result(left, right, rs.getLong("answer"))
        );
    }
}