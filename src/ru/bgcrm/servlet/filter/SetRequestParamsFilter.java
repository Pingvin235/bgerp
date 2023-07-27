package ru.bgcrm.servlet.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.db.sql.pool.ConnectionPool;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.app.servlet.Interface;
import org.bgerp.app.servlet.ServletUtils;
import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.app.servlet.jsp.UiFunction;

import ru.bgcrm.cache.CustomerGroupCache;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.cache.UserGroupRoleCache;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.process.TypeTreeItem;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.sql.ConnectionSet;

public class SetRequestParamsFilter implements Filter {
    public static final String REQUEST_KEY_LOCALIZER = "l";

    public void destroy() {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Map<String, Object> variables = getContextVariables((HttpServletRequest) request);
        Expression.setExpressionContextUtils(variables);

        for (Map.Entry<String, Object> me : variables.entrySet())
            request.setAttribute(me.getKey(), me.getValue());

        //TODO: Документировать и переместить в общую функцию
        ConnectionSet conSet = new ConnectionSet(Setup.getSetup().getConnectionPool(), true);
        request.setAttribute("ctxConSet", conSet);

        // 'l' object is set only for action calls and open URLs like /process/nnn, but not to JSP after them
        if (!ServletUtils.getRequestURI((HttpServletRequest) request).endsWith(".jsp") ||
                // exception is the 'usermob' interface, where JSP templates might be called directly
                Interface.USER_MOB.equals(Interface.getIface((HttpServletRequest) request)))
            request.setAttribute(REQUEST_KEY_LOCALIZER, getLocalizer(request));

        request.setAttribute("ui", UiFunction.INSTANCE);

        chain.doFilter(request, response);

        conSet.recycle();
    }

    private Localizer getLocalizer(ServletRequest request) {
        var result = Localization.getLocalizer((HttpServletRequest) request);
        if (result == null)
            result = Localization.getLocalizer();
        return result;
    }

    /**
     * Метод устанавливает в HttpRequest либо другой контекст следующие объекты:<br/>
     * <b>ctxSetup</b>  {@link Setup#getSetup} глобальная конфигурация<br/>
     *
     * <b>ctxCustomerGroupMap</b>  {@link CustomerGroupCache#getGroupMap()} Map с группами контрагентов<br/>
     * <b>ctxCustomerGroupList</b>  {@link CustomerGroupCache#getGroupList()} List с группами контрагентов<br/>
     *
     * <b>ctxUser</b>  {@link User} текущий пользователь<br/>
     *
     * <b>ctxPluginManager</b>  {@link PluginManager#getInstance()}<br/>
     *
     * <b>ctxUserCache</b> - {@link UserCache#INSTANCE} user cache instance</br>
     *
     * <b>ctxUserList</b>  {@link UserCache#getUserList()} List с пользователями системы<br/>
     * <b>ctxUserMap</b>  {@link UserCache#getUserMap()} Map с пользователями системы<br/>
     *
     * <b>ctxUserGroupRoleList</b>  {@link UserGroupRoleCache#getUserGroupRoleList()} List с ролями групп в процессах<br/>
     * <b>ctxUserGroupRoleMap</b>  {@link UserGroupRoleCache#getUserGroupRoleMap()} Map с ролями групп в процессах<br/>
     *
     * <b>ctxUserGroupList</b>  {@link UserCache#getUserGroupList()} List с группами пользователей<br/>
     * <b>ctxUserGroupMap</b>  {@link UserCache#getUserGroupMap()} Map с группами пользователей<br/>
     * <b>ctxUserGroupFullTitledList</b>  {@link UserCache#getUserGroupFullTitledList()} List с группами пользователей, наименования групп включают полный путь<br/>
     * <b>ctxUserGroupFullTitledMap</b>  {@link UserCache#getUserGroupFullTitledList()} Map с группами пользователей, наименования групп включают полный путь<br/>
     *
     * <b>ctxUserPermsetList</b>  {@link UserCache#getUserPermsetList()} List с наборами прав пользователей<br/>
     * <b>ctxUserPermsetMap</b>  {@link UserCache#getUserPermsetMap()} Map с наборами прав пользователей<br/>
     *
     * <b>ctxDataSource</b>  {@link Setup#getDataSource()} DataSource - пул соединений с БД для использования в JSP<br/>
     * <b>ctxSlaveDataSource</b>  {@link Setup#getSlaveDataSource()} DataSource - пул соединений с Slave БД для использования в JSP<br/>
     *
     * <b>ctxProcessTypeMap</b>  {@link ProcessTypeCache#getProcessTypeMap()} Map с типами процессов<br/>
     * <b>ctxProcessTypeTreeRoot</b> {@link TypeTreeItem} - корневой узел дерева процессов<br/>
     * <b>ctxProcessStatusList</b>  {@link ProcessTypeCache#getProcessStatusList()} List со статусами процессов<br/>
     * <b>ctxProcessStatusMap</b>  {@link ProcessTypeCache#getProcessStatusMap()} Map со статусами процессов<br/>
     * <b>ctxProcessQueueMap</b>  {@link ProcessTypeCache#getProcessQueueMap()} Map с очередями процессов<br/>
     * <b>ctxProcessQueueList</b>  {@link ProcessTypeCache#getProcessQueueList()} List с очередями процессов<br/>
     *
     * <b>ctxParameterMap</b>  {@link ParameterCache#getParameterMap()} Map с параметрами<br/>
     *
     * <br/>
     */
    public static Map<String, Object> getContextVariables(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>(30);

        // TODO: Убрать не ctx переменные в дальнейшем, после проверки, что нигде не используется.
        // Переменные ctx запрещено использовать в акшенах.
        result.put("ctxSetup", Setup.getSetup());
        result.put("setup", Setup.getSetup());

        result.put("ctxCustomerGroupMap", CustomerGroupCache.getGroupMap());
        result.put("ctxCustomerGroupList", CustomerGroupCache.getGroupList());

        if (request != null) {
            result.put("ctxUser", AuthFilter.getUser(request));
            result.put("ctxIface", Interface.getIface(request));
        }

        result.put("ctxPluginManager", PluginManager.getInstance());

        result.put("ctxUserCache", UserCache.INSTANCE);

        result.put("ctxUserList", UserCache.getUserList());
        result.put("ctxUserMap", UserCache.getUserMap());

        result.put("ctxUserGroupRoleList", UserGroupRoleCache.getUserGroupRoleList());
        result.put("ctxUserGroupRoleMap", UserGroupRoleCache.getUserGroupRoleMap());

        result.put("ctxUserGroupList", UserCache.getUserGroupList());
        result.put("ctxUserGroupMap", UserCache.getUserGroupMap());
        result.put("ctxUserGroupFullTitledList", UserCache.getUserGroupFullTitledList());
        result.put("ctxUserGroupFullTitledMap", UserCache.getUserGroupFullTitledMap());

        result.put("ctxUserPermsetList", UserCache.getUserPermsetList());
        result.put("ctxUserPermsetMap", UserCache.getUserPermsetMap());

        ConnectionPool connectionPool = Setup.getSetup().getConnectionPool();

        result.put("ctxDataSource", connectionPool.getDataSource());
        result.put("ctxSlaveDataSource", connectionPool.getSlaveDataSource());

        result.put("ctxProcessTypeMap", ProcessTypeCache.getProcessTypeMap());
        result.put("ctxProcessTypeTreeRoot", ProcessTypeCache.getTypeTreeRoot());
        result.put("ctxProcessStatusList", ProcessTypeCache.getStatusList());
        result.put("ctxProcessStatusMap", ProcessTypeCache.getStatusMap());
        result.put("ctxProcessQueueMap", ProcessQueueCache.getQueueMap());
        result.put("ctxProcessQueueList", ProcessQueueCache.getQueueList());

        result.put("ctxParameterMap", ParameterCache.getParameterMap());

        return result;
    }

    public void init(FilterConfig filterConfig) throws ServletException {}
}