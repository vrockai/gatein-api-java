/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.User;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.page.Page;
import org.gatein.api.portal.site.Site;
import org.gatein.api.util.Filter;

import java.util.Locale;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class PortalRequest
{
   /**
    * The current portal request user. If the request is for an unauthenticated user then {@link User#anonymous()}
    * should be returned.
    *
    * @return the user of the current portal request. This should never return null.
    */
   public abstract User getUser();

   /**
    * The current portal request Site Id.
    *
    * @return the Site Id of the current portal request. This should never return null.
    */
   public abstract Site.Id getSiteId();

   /**
    * The current portal request node path.
    *
    * @return the node path of the current portal request.
    */
   public abstract NodePath getNodePath();

   /**
    * The current portal request locale.
    *
    * @return the locale of the current portal request.
    */
   public abstract Locale getLocale();

   public Site getSite()
   {
      return getPortal().getSite(getSiteId());
   }

   public void save(Site site)
   {
      getPortal().saveSite(site);
   }

   public Page getPage()
   {
      Page.Id pageId = getNode().getPageId();

      return (pageId == null) ? null : getPortal().getPage(pageId);
   }

   public void save(Page page)
   {
      getPortal().savePage(page);
   }

   public Navigation getNavigation()
   {
      return getNavigation(1, Nodes.userFilter(getUser(), getPortal()));
   }

   public Navigation getNavigation(int depth, Filter<Node> filter)
   {
      Portal portal = getPortal();
      return portal.getNavigation(getSiteId(), Nodes.visitNodes(depth), filter);
   }

   public Node getNode() throws EntityNotFoundException
   {
      Node node = getPortal().getNode(getSiteId(), getNodePath());
      if (node == null) throw new EntityNotFoundException("Node could not be found for current request path " + getNodePath());

      return node;
   }

   public abstract Portal getPortal();

   public static PortalRequest getInstance()
   {
      return instance.get();
   }

   protected static void setInstance(PortalRequest request)
   {
      if (request == null)
      {
         instance.remove();
      }
      else
      {
         instance.set(request);
      }
   }

   private static ThreadLocal<PortalRequest> instance = new ThreadLocal<PortalRequest>()
   {
      @Override
      protected PortalRequest initialValue()
      {
         return null;
      }
   };
}