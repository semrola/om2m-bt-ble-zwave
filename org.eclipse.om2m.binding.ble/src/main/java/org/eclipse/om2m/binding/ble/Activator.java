package org.eclipse.om2m.binding.ble;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.interworking.service.InterworkingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Log logger = LogFactory.getLog(Activator.class);

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		logger.info("Registering BLE...");
		bundleContext.registerService(InterworkingService.class.getName(), new Controller(), null);
		logger.info("BLE is registered.");
		
		ServiceTracker cseServiceTracker = new ServiceTracker<Object, Object>(bundleContext, CseService.class.getName(), null) {
            public void removedService(ServiceReference<Object> reference, Object service) {
                logger.info("CseService removed");
            }

            public Object addingService(ServiceReference<Object> reference) {
                logger.info("CseService discovered");
                CseService cseService = (CseService) this.context.getService(reference);
                RequestSender.CSE = cseService;
                new Thread(){
                    public void run(){
                        try {                       	
                        	BleMonitor.start();
                        } catch (Exception e) {
                            logger.error("BLE Monitor error", e);
                        }
                    }
                }.start();
                return cseService;
            }
            
        };
        cseServiceTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
