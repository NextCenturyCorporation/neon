Exec { path => [ "/bin/", "/sbin/" , "/usr/bin/", "/usr/sbin/" ] }

class open8080 {
	exec { "addRule":
		command => "ufw allow 8080 && \
				ufw allow 22 && \
				ufw --force enable && \
				service ufw restart",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"]
	}
}

class update {
	exec { "update":
		command => "apt-get update"
	}
}

class java7 {
	package { "openjdk-7-jdk":
		ensure  => present,
		require => Class["update"]
	}
}

class mongodb {
	package { "mongodb":
		ensure => present,
		require => Class["update"]
	}

	service { "mongodb":
		ensure => "running",
		require => Package["mongodb"]
	}	
}

class tomcat7 {
	package { "tomcat7":
		ensure => "present",
		require => Class["java7", "update"]
	}

	service { "tomcat7":
		ensure => "running",
		require => Package["tomcat7"]
	}
}

class getNeon {
	package { "wget":
		ensure => "present",
		require => Class["update"]
	}

	exec { "wget http://neonframework.org/neon/versions/latest/neon.war":
		cwd => "/var/lib/tomcat7/webapps",
		creates => "/var/lib/tomcat7/webapps/neon.war",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"],
		require => Package["wget", "tomcat7"]
	}

	exec { "wget http://neonframework.org/neon-gtd/versions/latest/neon-examples.war":
		cwd => "/var/lib/tomcat7/webapps",
		creates => "/var/lib/tomcat7/webapps/neon-examples.war",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"],
		require => Package["wget", "tomcat7"]
	}
}

class restartTomcat {
	exec { "service tomcat7 restart":
		require => Class["tomcat7", "getNeon"]
	}
}

class getDemoData {
	package { "git":
		ensure => "present",
		require => Class["update"]
	}

	exec { "getData":
		command => "git clone https://github.com/NextCenturyCorporation/neon.git",
		creates => "/neon/",
		cwd => "/",
		require => Package["git"]
	}
}

class unzipDemoData {
	exec { "unzip":
		command => "jar -xvf examples/earthquakes.zip",
		cwd => "/neon",
		require => Class["getDemoData","java7"]
	}
}

class setupDemoData {
	exec { "importData":
		command => "mongoimport --db test --collection earthquakes --file earthquakes.csv --headerline --type csv && \
			mongo test --eval \"db.earthquakes.find().forEach(function(doc){doc.time = new ISODate(doc.time);db.earthquakes.save(doc)});\"",
		cwd => "/neon",
		require => Class["unzipDemoData","mongodb"]
	}
}

include open8080, update, java7, mongodb, tomcat7, getNeon, restartTomcat, getDemoData, unzipDemoData, setupDemoData
