// Licensed under Apache License version 2.0
// Original license LGPL

// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package ajouino.util;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Sample Code for Listing Services using JmDNS.
 * <p>
 * Run the main method of this class. This class prints a list of available HTTP services every 5 seconds.
 *
 * @author Werner Randelshofer
 */
public class JmDNSUtil {

    public final static String ARDUINO_SERVICE = "_arduino._tcp.local.";

    private static JmDNS jmDNS = null;
    {
        try {
            jmDNS = JmDNS.create();
            jmDNS.addServiceListener(ARDUINO_SERVICE, serviceListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ServiceListener serviceListener = new ServiceListener() {
        @Override
        public void serviceAdded(ServiceEvent serviceEvent) {
            serviceInfos.add(serviceEvent.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent serviceEvent) {
            serviceInfos.remove(serviceEvent.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent serviceEvent) {
            serviceInfos.add(serviceEvent.getInfo());
        }
    };

    private static Collection<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

    public static ServiceInfo[] lookupServices(String serviceType) {

        ServiceInfo[] infos = null;
        try {
            if(jmDNS == null) jmDNS = JmDNS.create();
            infos = jmDNS.list(ARDUINO_SERVICE);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            if (jmDNS != null) try {
//                jmDNS.close();
//            } catch (IOException exception) {
//                exception.printStackTrace();
//            }
        }
        return infos;
    }

}
