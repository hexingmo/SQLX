/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.sqlx.util;

import com.github.sqlx.NodeType;
import com.github.sqlx.jdbc.ProxyConnection;
import com.github.sqlx.listener.RouteInfo;


import java.sql.Connection;
import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public final class RoutingUtils {

    private RoutingUtils() {
        throw new IllegalAccessError("RoutingUtils does not support object instantiation");
    }

    public static void setDefaultDatabase(String database , RouteInfo routeInfo) {
        if (StringUtils.isNotBlank(database) && routeInfo != null && routeInfo.getSqlAttribute() != null) {
            routeInfo.getSqlAttribute().setDefaultDatabase(database);
        }
    }

    public static boolean isRoutingRead(Connection connection) {
        return isRoutingTarget(NodeType.READ , connection);
    }

    public static boolean isRoutingWrite(Connection connection) {
        return isRoutingTarget(NodeType.WRITE , connection);
    }

    public static boolean isRoutingTarget(NodeType targetMode , Connection connection) {
        if (Objects.isNull(connection)) {
            return false;
        }

        if (connection instanceof ProxyConnection) {
            // TODO 重构影响点
            ProxyConnection rc = (ProxyConnection) connection;
//            NodeType nodeType = rc.getRoutingInfoList().get(0).getHitNodeAttr().getNodeType();
//            if (Objects.isNull(nodeType)) {
//                return false;
//            }
//            return Objects.equals(targetMode, nodeType) || Objects.equals(NodeType.READ_WRITE, nodeType);
        }
        return false;
    }
}
