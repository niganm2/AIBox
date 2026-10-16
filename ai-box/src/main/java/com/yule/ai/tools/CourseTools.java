package com.yule.ai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.yule.ai.entity.po.Course;
import com.yule.ai.entity.po.CourseReservation;
import com.yule.ai.entity.po.School;
import com.yule.ai.entity.query.CourseQuery;
import com.yule.ai.service.ICourseReservationService;
import com.yule.ai.service.ICourseService;
import com.yule.ai.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CourseTools {

    // 课程服务
    private final ICourseService courseService;

    // 学校服务
    private final ISchoolService schoolService;

    // 课程预约服务
    private final ICourseReservationService courseReservationService;

    @Tool(description = "根据条件查询课程信息")
    public List<Course> queryCourses(
            @ToolParam(description = "查询的条件", required = false) CourseQuery query
    ) {
        if (query == null) {
            return List.of();
        }

        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType()) //type = '编程'
                .le(query.getEdu() != null, "edu", query.getEdu());//edu <= 2

        if (query.getSorts() != null && !query.getSorts().isEmpty()) {
            for (CourseQuery.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }
        return wrapper.list();
    }

    @Tool(description = "查询所有校区")
    public List<School> querySchools() {
        return schoolService.list();
    }

    @Tool(description = "生成预约单，返回预约单号")
    public Integer createCourseReservation(
            @ToolParam(description = "预约课程") String course,
            @ToolParam(description = "预约校区") String school,
            @ToolParam(description = "学生姓名") String studentName,
            @ToolParam(description = "联系电话") String contactInfo,
            @ToolParam(description = "备注", required = false) String remark) {
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(course);
        courseReservation.setSchool(school);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setRemark(remark);
        courseReservationService.save(courseReservation);

        return courseReservation.getId();
    }
}
