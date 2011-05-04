/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.api;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import org.gatein.api.id.Common;
import org.gatein.api.id.Id;
import org.gatein.api.navigation.Dashboard;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.navigation.Page;
import org.gatein.api.organization.Operation;
import org.gatein.api.organization.Users;
import org.gatein.api.navigation.Window;
import org.gatein.api.organization.Group;
import org.gatein.api.organization.Groups;
import org.gatein.api.organization.User;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class NavigationPortletTestCase
{
   @Test(enabled = false)
   public void shouldListGroupPages()
   {
      Id<User> id = Common.getUserId("root");
      User root = Users.get(id);

      List<Group> rootGroups = root.getGroups();

      Id<Group> groupId = Common.getGroupId("platform", "administrators");
      final Group adminGroup = root.getGroup(groupId);
      assert rootGroups.contains(adminGroup);

      List<Node> adminNodes = Nodes.get(new Nodes.GroupNodeFilter(groupId));
      assert 2 == adminNodes.size();
      Node administrationNode = adminNodes.get(0);
      assert "Administration".equals(administrationNode.getDisplayName());
      assert 2 == administrationNode.getChildren().size();
      Node wsrp = adminNodes.get(1);
      assert "WSRP".equals(wsrp.getDisplayName());
      assert wsrp instanceof Page;
      Page wsrpPage = (Page)wsrp;
      List<Node> wsrpChildren = wsrp.getChildren();
      assert 1 == wsrpChildren.size();
      Node wsrpWindow = wsrpChildren.get(0);
      assert wsrpWindow instanceof Window;
      assert wsrpWindow.equals(wsrpPage.getWindow(wsrpWindow.getName(), false));
      assert wsrpWindow.equals(Nodes.get(wsrpWindow.getId()));


      Group executiveGroup = Groups.get(Common.getGroupId("organization", "management", "executive-board"));
      assert rootGroups.contains(executiveGroup);

      Group userGroup = Groups.get(Common.getGroupId("platform", "users"));
      assert rootGroups.contains(userGroup);
   }

   @Test(enabled = false)
   public void shouldListSitePages()
   {
      Id<User> id = Common.getUserId("root");
      final User root = Users.get(id);

      // from user
      List<Portal> portals = root.getPortals();

      // from container
      Collection<PortalContainer> containers = GateIn.getPortalContainers();
      List<Portal> fromContainers = new ArrayList<Portal>();
      for (PortalContainer container : containers)
      {
         Collection<Portal> userPortals = container.get(new Filter<Portal>()
         {
            public boolean accept(Portal item)
            {
               return item.accessAllowedFrom(root, Operation.READ);
            }
         });
         fromContainers.addAll(userPortals);
      }
      assert portals.equals(fromContainers);

      // from Nodes
      Collection<Portal> fromNodes = Nodes.getForUser(root.getId(), Portal.class);
      assert portals.equals(fromNodes);

      for (Portal portal : portals)
      {
         List<Node> children = portal.getChildren(new Filter<Node>()
         {
            public boolean accept(Node item)
            {
               return item.accessAllowedFrom(root, Operation.READ);
            }
         });
         // then display
      }
   }

   @Test(enabled = false)
   public void shouldListDashboardPages()
   {
      Id<User> id = Common.getUserId("root");
      final User root = Users.get(id);

      Filter<Dashboard> filter = new Filter<Dashboard>()
      {
         public boolean accept(Dashboard item)
         {
            return item.accessAllowedFrom(root, Operation.READ);
         }
      };
      List<Dashboard> nodes = Nodes.get(filter);
      assert 1 == nodes.size();
      Dashboard dashboard = Nodes.getSingleOrFail(filter);
      assert dashboard.equals(nodes.get(0));

      // from Nodes
      Collection<Dashboard> fromNodes = Nodes.getForUser(root.getId(), Dashboard.class);
      assert nodes.equals(fromNodes);
      assert dashboard.equals(fromNodes.iterator().next());
   }
}
