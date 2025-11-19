package com.pavlov.workout.api;

import com.pavlov.workout.entity.Workout;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorkoutTest extends AbstractWebTest {

    static final String URL = "/api/v1/workouts";

    @Test
    void createTest() throws Exception {
        String workout = """
            { "userId": 1, "name": "день 1", "calories": 400, "activityType": 1, "planType": 2, "planedAt": "2025-11-19T11:48:23.890Z", "duration": "PT1H30M5S" }""";
        MvcResult res = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workout))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andDo(print())
                .andReturn();
        Workout workoutResponse = objectMapper.readValue(res.getResponse().getContentAsByteArray(), Workout.class);
        System.out.println("!!! Duration is : " + workoutResponse.getDuration());
        System.out.println("!!! ID is : " + workoutResponse.getId());
        Long id = workoutResponse.getId();

        res = mockMvc.perform(get(URL + "/" + 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        workoutResponse = objectMapper.readValue(res.getResponse().getContentAsByteArray(), Workout.class);
        System.out.println("!!! Duration is : " + workoutResponse.getDuration());
    }

}
