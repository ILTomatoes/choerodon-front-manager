package io.choerodon.manager.api.controller.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.swagger.web.SwaggerResource;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.manager.api.dto.swagger.ControllerDTO;
import io.choerodon.manager.app.service.ApiService;
import io.choerodon.manager.app.service.SwaggerService;
import io.choerodon.manager.infra.common.utils.VersionUtil;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author superlee
 */
@RestController
@RequestMapping(value = "/v1/swaggers")
public class ApiController {

    private SwaggerService swaggerService;

    private ApiService apiService;

    public ApiController(SwaggerService swaggerService, ApiService apiService) {
        this.swaggerService = swaggerService;
        this.apiService = apiService;
    }

    public void setSwaggerService(SwaggerService swaggerService) {
        this.swaggerService = swaggerService;
    }

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("查询不包含跳过的服务的路由列表")
    @GetMapping("/resources")
    public ResponseEntity<List<SwaggerResource>> resources() {
        return new ResponseEntity<>(swaggerService.getSwaggerResource(), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("查询服务controller和接口")
    @GetMapping("/{service_prefix}/controllers")
    @CustomPageRequest
    public ResponseEntity<Page<ControllerDTO>> queryByNameAndVersion(
            @PathVariable("service_prefix") String serviceName,
            @RequestParam(value = "version", required = false, defaultValue = VersionUtil.NULL_VERSION) String version,
            @RequestParam(required = false, name = "params") String params,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "description") String description,
            @ApiIgnore @SortDefault(value = "name", direction = Sort.Direction.ASC)
                    PageRequest pageRequest) {
        Map<String, Object> map = new HashMap<>();
        map.put("params", params);
        map.put("name", name);
        map.put("description", description);
        return new ResponseEntity<>(apiService.getControllers(serviceName, version, pageRequest, map), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("根据path的url和method查询单个path")
    @GetMapping("/{service_prefix}/controllers/{name}/paths")
    public ResponseEntity<ControllerDTO> queryPathDetail(@PathVariable("service_prefix") String serviceName,
                                                         @PathVariable("name") String controllerName,
                                                         @RequestParam(value = "version", required = false, defaultValue = VersionUtil.NULL_VERSION) String version,
                                                         @RequestParam("operation_id") String operationId) {
        return new ResponseEntity<>(apiService.queryPathDetail(serviceName, version, controllerName, operationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("查询运行的服务实例的api接口数量")
    @GetMapping("/api/count")
    public ResponseEntity<Map<String, Object>> queryInstancesAndApiCount() {
        return new ResponseEntity<>(apiService.queryInstancesAndApiCount(), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("根据日期查询服务的调用次数")
    @GetMapping("/service_invoke/count")
    public ResponseEntity<Map<String, Object>> queryServiceInvoke(@RequestParam(value = "begin_date")
                                                                  @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String beginDate,
                                                                  @RequestParam(value = "end_date")
                                                                  @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String endDate) {
        return new ResponseEntity<>(apiService.queryInvokeCount(beginDate, endDate, "", "service"), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("查询日期或者服务名或path路径查询api的调用次数")
    @GetMapping("/api_invoke/count")
    public ResponseEntity<Map<String, Object>> queryApiInvoke(@RequestParam(value = "begin_date")
                                                              @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String beginDate,
                                                              @RequestParam(value = "end_date")
                                                              @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String endDate,
                                                              @RequestParam String service) {
        return new ResponseEntity<>(apiService.queryInvokeCount(beginDate, endDate, service, "api"), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("查询树形接口目录")
    @GetMapping("/tree")
    public ResponseEntity<Map> queryTreeMenu() {
        return new ResponseEntity<>(apiService.queryTreeMenu(), HttpStatus.OK);
    }


}
