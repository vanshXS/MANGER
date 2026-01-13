package com.vansh.manger.Manger.DTO;

import com.vansh.manger.Manger.Entity.ClassroomStatus;
import com.vansh.manger.Manger.Entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.convert.DataSizeUnit;

import java.util.List;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ClassroomResponseDTO {

    Long id;
    private String name;
    private Integer capacity;
    private Long studentCount;
    private ClassroomStatus status;

}
