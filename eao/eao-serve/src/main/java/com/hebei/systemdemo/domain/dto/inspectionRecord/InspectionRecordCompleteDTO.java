package com.hebei.systemdemo.domain.dto.inspectionRecord;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRecordCompleteDTO {
    @NotEmpty(message = "完工记录不能为空")
    private List<Long> ids;
}
