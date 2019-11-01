package io.choerodon.manager.api.dto;

import io.choerodon.manager.api.validator.Check;
import io.choerodon.manager.api.validator.Insert;
import io.choerodon.manager.api.validator.Update;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

import static io.choerodon.manager.infra.utils.RegularExpression.ALPHANUMERIC_AND_SPACE_SYMBOLS;
import static io.choerodon.manager.infra.utils.RegularExpression.ROUTE_RULE_ALL_SYMBOLS_200;

/**
 * RouteRuleVO
 *
 * @author pengyuhua
 * @date 2019/10/25
 */
public class RouteRuleVO {
    @ApiModelProperty(value = "主键ID")
    @NotNull(message = "error.route.rule.update.id.can.not.be.null", groups = {Update.class})
    private Long id;
    @ApiModelProperty(value = "路由编码/必填")
    @NotEmpty(message = "error.route.rule.code.can.not.be.empty", groups = {Insert.class, Check.class})
    @Pattern(regexp = ALPHANUMERIC_AND_SPACE_SYMBOLS,message = "error.route.rule.code.format.incorrect", groups = {Insert.class, Check.class})
    private String code;
    @ApiModelProperty(value = "路由描述/选填")
    @Pattern(regexp = ROUTE_RULE_ALL_SYMBOLS_200, message = "error.route.rule.description.format.incorrect", groups = {Insert.class, Update.class})
    private String description;

    @ApiModelProperty(value = "该路由下配置的用户信息")
    @Valid
    private List<RouteMemberRuleDTO> routeMemberRuleDTOS;

    private Long objectVersionNumber;
    private Date creationDate;
    private Long userNumber;

    public Long getId() {
        return id;
    }

    public RouteRuleVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public RouteRuleVO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RouteRuleVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<RouteMemberRuleDTO> getRouteMemberRuleDTOS() {
        return routeMemberRuleDTOS;
    }

    public RouteRuleVO setRouteMemberRuleDTOS(List<RouteMemberRuleDTO> routeMemberRuleDTOS) {
        this.routeMemberRuleDTOS = routeMemberRuleDTOS;
        return this;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public RouteRuleVO setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
        return this;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public RouteRuleVO setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Long getUserNumber() {
        return userNumber;
    }

    public RouteRuleVO setUserNumber(Long userNumber) {
        this.userNumber = userNumber;
        return this;
    }
}

