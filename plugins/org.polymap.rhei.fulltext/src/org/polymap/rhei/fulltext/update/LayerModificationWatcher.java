/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.fulltext.update;

/**
 * Listen to changes of the layer and its content and update the index appropriately.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerModificationWatcher {
//        extends FulltextIndexUpdater {
//
//    private static Log log = LogFactory.getLog( LayerModificationWatcher.class );
//    
//    private SessionHolder           session;
//    
//    private ILayer                  layer;
//
//    private UpdateableFulltextIndex index;
//    
//   
//    public LayerModificationWatcher( SessionHolder session, UpdateableFulltextIndex index, ILayer layer ) {
//        this.session = session;
//        this.index = index;
//        this.layer = layer;
//    }
//
//    
//    @Override
//    protected UpdateableFulltextIndex index() {
//        return index;
//    }
//
//    
//    public void start() {
//        super.start( session.getContext() );
//    }
//    
//    
//    @EventHandler(scope=Event.Scope.JVM)
//    protected void modelChanged( EntityStateEvent ev ) {
//        if (ev.getEventType() == EventType.COMMIT) {
//
//            // any feature of the layer changed?
//            EntityHandle layerHandle = FeatureStateTracker.layerHandle( layer );
//
//            if (ev.hasChanged( (IEntityHandleable)layer ) || ev.hasChanged( layerHandle )) {                
//                EntityStateTracker.instance().removeListener( this );
//                session.dropServiceContext();
//                EntityStateTracker.instance().addListener( this );
//
//                start();
//                return;
//            }
//            // check parent maps for changes
//            IMap map = layer.getMap();
//            if (ev.hasChanged( (IEntityHandleable)map )) {
//                EntityStateTracker.instance().removeListener( this );
//                session.dropServiceContext();
//                EntityStateTracker.instance().addListener( this );
//
//                start();
//                return;
//            }
//        }
//    }
//
//    
//    @Override
//    protected void doUpdateIndex( Updater updater, IProgressMonitor monitor ) throws Exception {
//        PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
//        FeatureCollection fc = null;
//        Iterator it = null;
//        
//        try {
//            log.info( layer.getLabel() + ": indexing..." );
//            // CRS/SRS
//            CoordinateReferenceSystem dataCRS = fs.getSchema().getCoordinateReferenceSystem();
//            //String dataSRS = layer.getCRSCode();
//            if (dataCRS == null) {
//                dataCRS = Geometries.crs( layer.getMap().getCRSCode() );
//            }
//            String dataSRS = Geometries.srs( dataCRS );
//            log.debug( "    " + layer.getLabel() + ": found FeatureSource: " + fs + ", SRS=" + dataSRS );
//            
//            // start indexing
//            fc = fs.getFeatures();
//            it = fc.iterator();
//            monitor.beginTask( layer.getLabel(), IProgressMonitor.UNKNOWN );
//            
//            // FIXME delete removed features
//            int count = 0;
//            while (it.hasNext() && !monitor.isCanceled()) {
//                Feature feature = (Feature)it.next();
//                JSONObject transformed = transform( feature );
//                updater.store( transformed, true );
//                
//                if (count++ % 100 == 0) {
//                    monitor.worked( 100 );
//                }
//            }
//        }
//        catch (Exception e) {
//            log.warn( "Fehler beim Indizieren:" + e.getLocalizedMessage(), e );
//            //log.debug( e.getLocalizedMessage(), e );
//            throw e;
//        }
//        finally {
//            if (fc != null && it != null) {
//                fc.close( it );
//            }
//            monitor.done();
//        }
//    }
    
}
