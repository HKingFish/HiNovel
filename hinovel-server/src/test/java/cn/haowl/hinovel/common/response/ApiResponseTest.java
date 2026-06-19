package cn.haowl.hinovel.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResponse 单元测试
 * 验证统一响应体的构造、序列化等核心行为
 */
class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void success_shouldReturn200WithData() {
        ApiResponse<String> response = ApiResponse.success("hello");
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getMessage()).isEqualTo("操作成功");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_noData_shouldReturn200WithNullData() {
        ApiResponse<Void> response = ApiResponse.success();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNull();
    }

    @Test
    void error_withErrorCode_shouldReturnCorrectCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.EMAIL_ALREADY_EXISTS);
        assertThat(response.getCode()).isEqualTo(40002);
        assertThat(response.getMessage()).isEqualTo("邮箱已被注册");
    }

    @Test
    void error_withCustomMessage_shouldOverrideDefaultMessage() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.PARAM_ERROR, "自定义错误消息");
        assertThat(response.getCode()).isEqualTo(40001);
        assertThat(response.getMessage()).isEqualTo("自定义错误消息");
    }

    @Test
    void serialization_shouldNotIncludeNullFields() throws Exception {
        ApiResponse<Void> response = ApiResponse.success();
        String json = objectMapper.writeValueAsString(response);
        // data 为 null，不应出现在 JSON 中（@JsonInclude(NON_NULL)）
        assertThat(json).doesNotContain("\"data\"");
        assertThat(json).contains("\"code\"");
        assertThat(json).contains("\"message\"");
    }
}
