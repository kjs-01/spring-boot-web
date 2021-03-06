package com.apress.spring;

import com.apress.spring.domain.JournalEntry;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.iterableWithSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpringBootWebApplicationTests {

    private final String SPRING_BOOT_MATCH = "스프링 부트";
    private final String CLOUD_MATCH = "클라우드";

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    private MockMvc mockMvc;


    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters){
        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
            .filter(converter -> converter instanceof  MappingJackson2HttpMessageConverter)
            .findAny().get();
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                        .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                        .build();
    }

    @Test
    public void getAll() throws Exception {
        mockMvc.perform(get("/journal/all"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$",iterableWithSize(5)))
                .andExpect(jsonPath("$[0]['title']",containsString(SPRING_BOOT_MATCH)));
    }

    public void testGetAll() throws Exception {
        mockMvc.perform(get("/journal/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", iterableWithSize(4)))
                .andExpect(jsonPath("$[0].title", containsString(SPRING_BOOT_MATCH)));
    }

    @Test
    public void findByTitle() throws Exception {
        mockMvc.perform(get("/journal/findBy/title/"+CLOUD_MATCH))
                // 처리 내용을 출력합니다.
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$",iterableWithSize(1)))
                .andExpect(jsonPath("$[0]['title']",containsString(CLOUD_MATCH)));
    }

    @Test
    public void add() throws Exception {
        mockMvc.perform(post("/journal")
                .content(this.toJsonString(new JournalEntry("스프링 부트 테스트","스프링 부트 단위 테스트를 생성했다","05/09/2016")))
                .contentType(contentType))
                .andExpect(status().isOk());
    }


    @SuppressWarnings("unchecked")
    protected String toJsonString(Object obj) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(obj, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }



}