package com.moji.dto;

import com.moji.po.At;
import com.moji.po.Comments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddComment {

    private Comments comments;
    private List<At> ats;
}
