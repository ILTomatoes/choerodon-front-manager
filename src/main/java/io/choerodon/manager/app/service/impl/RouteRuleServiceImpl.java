package io.choerodon.manager.app.service.impl;import com.fasterxml.jackson.core.JsonProcessingException;import com.fasterxml.jackson.databind.ObjectMapper;import com.github.pagehelper.PageInfo;import io.choerodon.core.exception.CommonException;import io.choerodon.manager.api.dto.HostDTO;import io.choerodon.manager.api.dto.RouteRuleDTO;import io.choerodon.manager.api.dto.RouteRuleVO;import io.choerodon.manager.app.service.HostService;import io.choerodon.manager.app.service.RouteRuleService;import io.choerodon.manager.infra.feign.IamClient;import io.choerodon.manager.infra.retrofit.GoRegisterRetrofitClient;import okhttp3.ResponseBody;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.data.domain.Pageable;import org.springframework.http.ResponseEntity;import org.springframework.stereotype.Service;import org.springframework.util.CollectionUtils;import org.springframework.util.ObjectUtils;import retrofit2.Call;import retrofit2.Response;import java.io.IOException;import java.util.*;import java.util.stream.Collectors;import java.util.stream.Stream;import static io.choerodon.manager.app.service.impl.HostServiceImpl.ROUTE_RULE_CODE;/** * RouteRuleServiceImpl * * @author pengyuhua * @date 2019/10/25 */@Servicepublic class RouteRuleServiceImpl implements RouteRuleService {    private IamClient iamClient;    private HostService hostService;    private GoRegisterRetrofitClient goRegisterRetrofitClient;    private ObjectMapper objectMapper;    private static final Logger LOGGER = LoggerFactory.getLogger(RouteRuleServiceImpl.class);    public RouteRuleServiceImpl(IamClient iamClient, HostService hostService, GoRegisterRetrofitClient goRegisterRetrofitClient, ObjectMapper objectMapper) {        this.iamClient = iamClient;        this.hostService = hostService;        this.goRegisterRetrofitClient = goRegisterRetrofitClient;        this.objectMapper = objectMapper;    }    @Override    public PageInfo<RouteRuleVO> listRouteRules(Pageable pageable, String code) {        PageInfo<RouteRuleVO> pageInfo = new PageInfo<>();        // 查询路由信息        ResponseEntity<PageInfo<RouteRuleVO>> pageInfoResponseEntity = iamClient.listRouteRules(pageable, code);        if (pageInfoResponseEntity == null) {            pageInfo.setPageSize(pageable.getPageSize());            pageInfo.setPageNum(pageable.getPageNumber());            pageInfo.setList(new ArrayList<>());            return pageInfo;        }        // 查询每个路由下的主机信息        PageInfo<RouteRuleVO> routeRuleVOPageInfo = pageInfoResponseEntity.getBody();        routeRuleVOPageInfo.getList().forEach(v -> {            List<HostDTO> collect = hostService.listHosts().parallelStream().filter(va -> v.getCode().equals(va.getRouteRuleCode())).collect(Collectors.toList());            v.setHostNumber(collect.stream().count());            v.setHostDTOS(collect);        });        return routeRuleVOPageInfo;    }    @Override    public RouteRuleVO queryRouteRuleDetailById(Long id) {        // 查询base 路由用户关联信息        RouteRuleVO routeRuleVO = checkRouteRuleExist(id);        // 查询 路由主机关联信息        routeRuleVO.setHostDTOS(hostService.listHosts().parallelStream().filter(va -> routeRuleVO.getCode().equals(va.getRouteRuleCode())).collect(Collectors.toList()));        return routeRuleVO;    }    @Override    public RouteRuleVO insertRouteRule(RouteRuleVO routeRuleVO) {        // 更新路由及关联用户信息        ResponseEntity<RouteRuleVO> routeRuleVOResponseEntity = iamClient.insertRouteRule(routeRuleVO);        if (routeRuleVOResponseEntity == null) {            throw new CommonException("error.route.rule.insert");        }        // 路由配置主机信息 构造加入数据        Map<String, Map<String, String>> insertData= new HashMap<>();        Map<String, String> insertSubData = new HashMap<>();        if (!ObjectUtils.isEmpty(routeRuleVO.getInstanceIds())){            Arrays.stream(routeRuleVO.getInstanceIds()).forEach(v -> {                insertSubData.put(ROUTE_RULE_CODE, routeRuleVO.getCode());                insertData.put(v, insertSubData);            });            try {                LOGGER.error(objectMapper.writeValueAsString(insertData));            } catch (JsonProcessingException e) {                LOGGER.error(e.toString());            }            // 添加主机数据信息            Call<ResponseBody> call = goRegisterRetrofitClient.updateApps(insertData);            executeRetrofitCall(call, "error.route.host.config.fail");        }        return routeRuleVOResponseEntity.getBody();    }    @Override    public Boolean deleteRouteRuleById(Long id) {        // 查询路由配置的用户以及主机信息        RouteRuleVO routeRuleVO = queryRouteRuleDetailById(id);        // 删除路由及关联用户信息        ResponseEntity<Boolean> booleanResponseEntity = iamClient.deleteRouteRuleById(id);        // 删除路由配置主机信息 删除主机的routeRuleCode 标签        Map<String, Map<String, String>> insertData= new HashMap<>();        Map<String, String> insertSubData = new HashMap<>();        routeRuleVO.getHostDTOS().forEach(v -> {            insertSubData.put(ROUTE_RULE_CODE, "");            insertData.put(v.getInstanceId(), insertSubData);        });        // 删除主机标签        Call<ResponseBody> call = goRegisterRetrofitClient.updateApps(insertData);        executeRetrofitCall(call, "error.route.host.delete.fail");        return booleanResponseEntity.getBody();    }    @Override    public RouteRuleVO updateRouteRule(RouteRuleVO routeRuleVO) {        // 更新路由及关联用户信息        ResponseEntity<RouteRuleVO> routeRuleVOResponseEntity = iamClient.updateRouteRule(routeRuleVO);        if (routeRuleVOResponseEntity == null) {            throw new CommonException("error.route.rule.update");        }        RouteRuleVO routeRuleVOUpdated = checkRouteRuleExist(routeRuleVO.getId());        // 更新配置主机信息        Map<String, Map<String, String>> insertData= new HashMap<>();        // 1.获取当前已配置主机instanceID        RouteRuleVO routeRuleVOCheck = queryRouteRuleDetailById(routeRuleVO.getId());        // 2.过滤出需要清除的主机ID        // 3.重新配置需要配的置主机        if (!CollectionUtils.isEmpty(routeRuleVOCheck.getHostDTOS())) {            // 传入数据中不包含 "instanceIds" 键情况排除            String[] newInstanceIds = {};            if (ObjectUtils.isEmpty(routeRuleVO.getInstanceIds())) {                routeRuleVO.setInstanceIds(newInstanceIds);            }            // 构造需要删除的主机信息            routeRuleVOCheck.getHostDTOS().stream().filter(v -> v.getRouteRuleCode().equals(routeRuleVOCheck.getCode())).forEach(va -> {                    Arrays.asList(routeRuleVO.getInstanceIds()).forEach(v -> {                        if (!va.getInstanceId().equals(v)) {                            Map<String, String> insertSubData = new HashMap<>();                            insertSubData.put(ROUTE_RULE_CODE, "");                            insertData.put(va.getInstanceId(), insertSubData);                        }                    });                });        }        if (!ObjectUtils.isEmpty(routeRuleVO.getInstanceIds())) {            Arrays.asList(routeRuleVO.getInstanceIds()).forEach(v -> {                Map<String, String> insertSubData = new HashMap<>();                insertSubData.put(ROUTE_RULE_CODE, routeRuleVOUpdated.getCode());                insertData.put(v, insertSubData);            });        }        // 更新        Call<ResponseBody> call = goRegisterRetrofitClient.updateApps(insertData);        executeRetrofitCall(call, "error.route.host.update.fail");        return routeRuleVOResponseEntity.getBody();    }    @Override    public Boolean checkCode(RouteRuleVO routeRuleVO) {        return iamClient.checkCode(new RouteRuleDTO().setCode(routeRuleVO.getCode())).getBody();    }    private void executeRetrofitCall(Call<ResponseBody> call, String erroMsg) {        try {            Response<ResponseBody> execute = call.execute();            LOGGER.error(execute.toString());            if (!execute.isSuccessful()) {                throw new CommonException(erroMsg);            }        } catch (IOException e) {            throw new CommonException(erroMsg);        }    }    // 根据routeRuleID检查路由是否存在    private RouteRuleVO checkRouteRuleExist(Long id) {        ResponseEntity<RouteRuleVO> routeRuleVOResponseEntity = iamClient.queryRouteRuleDetailById(id);        if (routeRuleVOResponseEntity == null || routeRuleVOResponseEntity.getBody() == null) {            throw new CommonException("error.route.rule.query.fail");        }        return routeRuleVOResponseEntity.getBody();    }}