
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
	WatchService watchService = FileSystems.getDefault().newWatchService()
	private final TransformerRegistry registry
	private final Path path
	private final Map<String, String> loadedTransforms = [:]

	public TransformLoader(Path watchPath, TransformerRegistry transformRegistry) {
		registry = transformRegistry
		path = watchPath
		path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)
	}

	public void run() {
		for ( ; ; ) {
			WatchKey key = watchService.take()
			for ( WatchEvent<?> event: key.pollEvents()){
				handleEvent(event)
			}
			boolean valid = key.reset()
			if ( !valid ) {
				break
			}
		}
	}

	private void handleEvent(WatchEvent<?> event) {
		WatchEvent.Kind kind = event.kind()
		switch (kind.name()){
			case "ENTRY_MODIFY":
				if(event.context().toString().contains(".groovy")) {
					replaceTransform(event.context().toString())
				}
				break
			case "ENTRY_DELETE":
				if(event.context().toString().contains(".groovy")) {
					removeTransform(event.context().toString())
				}
				break
		}
	}

	@SuppressWarnings("JavaIoPackageAccess")
	private void loadTransform(String relativePath) {
		def file = new File(path.toString(), relativePath)

		GroovyClassLoader loader = new GroovyClassLoader()
		Transformer transform = loader.parseClass(file).newInstance()
		loadedTransforms.put(relativePath, transform.getName())
		registry.register(transform)
	}

	private void replaceTransform(String relativePath) {
		removeTransform(relativePath)
		loadTransform(relativePath)
	}

	private void removeTransform(String relativePath) {
		String name = loadedTransforms.get(relativePath)
		if(name) {
			Transformer transform = registry.removeTransformer(name)
			GroovySystem.getMetaClassRegistry().removeMetaClass(transform.getClass())
		}
	}
}