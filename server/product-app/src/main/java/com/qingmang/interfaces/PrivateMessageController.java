package com.qingmang.interfaces;

import com.qingmang.application.PrivateMessageService;
import com.qingmang.common.api.ApiResponse;
import com.qingmang.support.AuthContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privateMessage")
public class PrivateMessageController {
    private final PrivateMessageService service;

    public PrivateMessageController(PrivateMessageService service) { this.service = service; }

    public record DialogueRequest(@NotNull @Positive Long dialogueId) {}
    public record SendRequest(@NotNull @Positive Long receiverId, @NotNull Integer messageType,
                              @NotBlank @Size(max = 2000) String content) {}

    @GetMapping("/selectDialogue")
    public ApiResponse<List<Map<String, Object>>> dialogues(@RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(required = false) Long dialogueId) {
        return ApiResponse.ok(service.dialogues(AuthContext.requireUserId(), pageNum, dialogueId));
    }

    @PostMapping("/addDialogue")
    public ApiResponse<Long> add(@Valid @RequestBody DialogueRequest request) {
        return ApiResponse.ok(service.addDialogue(AuthContext.requireUserId(), request.dialogueId()));
    }

    @GetMapping("/selectMessage")
    public ApiResponse<Map<String, Object>> messages(@RequestParam Long dialogueId,
                                                    @RequestParam(defaultValue = "1") int pageNum) {
        return ApiResponse.ok(service.messages(AuthContext.requireUserId(), dialogueId, pageNum));
    }

    @PostMapping("/sendMessage")
    public ApiResponse<Map<String, Object>> send(@Valid @RequestBody SendRequest request) {
        return ApiResponse.ok(service.send(AuthContext.requireUserId(), request.receiverId(),
                request.messageType(), request.content()));
    }

    @PutMapping("/putMessageStatus")
    public ApiResponse<Void> read(@RequestParam Long dialogueId) {
        service.read(AuthContext.requireUserId(), dialogueId);
        return ApiResponse.ok();
    }

    @PutMapping("/putUpStatus")
    public ApiResponse<Void> pin(@RequestParam Long id) {
        service.updateDialogue(AuthContext.requireUserId(), id, "pin");
        return ApiResponse.ok();
    }

    @PutMapping("/putDndStatus")
    public ApiResponse<Void> mute(@RequestParam Long id) {
        service.updateDialogue(AuthContext.requireUserId(), id, "mute");
        return ApiResponse.ok();
    }

    @DeleteMapping("/deleteDialogue")
    public ApiResponse<Void> deleteDialogue(@RequestParam Long id) {
        service.updateDialogue(AuthContext.requireUserId(), id, "delete");
        return ApiResponse.ok();
    }

    @DeleteMapping("/revocationMessage")
    public ApiResponse<Void> revoke(@RequestParam Long id) {
        service.revoke(AuthContext.requireUserId(), id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/deleteMessage")
    public ApiResponse<Void> delete(@RequestParam Long id) {
        service.deleteMessage(AuthContext.requireUserId(), id);
        return ApiResponse.ok();
    }
}
