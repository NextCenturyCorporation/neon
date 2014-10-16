
package com.ncc.neon.config

import com.ncc.neon.query.result.Transformer
import com.ncc.neon.query.result.TransformerRegistry
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.Path
import java.nio.file.WatchService
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds

class TransformLoader implements Runnable {
	WatchService watchService = FileSystems.getDefault().newWatchService();
	private TransformerRegistry registry;
	private Path path;

	public TransformLoader(Path watchPath, TransformerRegistry transformRegistry) {
		registry = transformRegistry;
		path = watchPath;
		path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
	}

	public void run() {
		for ( ; ; ) {
			WatchKey key = watchService.take()

			//Poll all the events queued for the key
			for ( WatchEvent<?> event: key.pollEvents()){
				WatchEvent.Kind kind = event.kind()
				switch (kind.name()){
					case "ENTRY_CREATE":
						//IF groovy file
						System.out.println("Created: " + event.context());
					case "ENTRY_MODIFY":
						System.out.println("Modified: "+event.context());
						break
					case "ENTRY_DELETE":
						System.out.println("Delete: "+event.context());
						break
				}
			}

			//reset is invoked to put the key back to ready state
			boolean valid = key.reset()
			//If the key is invalid, just exit.

			if ( !valid ) {
				break
			}
		}
	}

	private void loadTransform(Path path) {
		@SuppressWarnings('JavaIoPackageAccess')
		def dir = new File(path.getPath())
		GroovyClassLoader loader = new GroovyClassLoader()
		Transformer transform = loader.parseClass(file).newInstance();
		registry.register(transform)
	}

	private void replaceTransform(Path path) {

	}

	private void removeTransform(Path path) {

	}
}