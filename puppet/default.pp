Exec { path => [ "/bin/", "/sbin/" , "/usr/bin/", "/usr/sbin/" ] }

class open8080 {
	exec { "addRule":
		command => "iptables -I INPUT 1 -p tcp --dport 8080 -j ACCEPT && \
				service iptables save && \
				service iptables restart",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"]
	}
}

class java7 {
	package { "java-1.7.0-openjdk.x86_64":
		ensure  => present
	}
	
	package { "java-1.7.0-openjdk-devel":
		ensure => present
	}
}

class mongodb {
	package { "mongodb-org":
		ensure => present
	}

	service { "mongod":
		ensure => "running",
		require => Package["mongodb-org"]
	}	
}

class tomcat7 {
	package { "tomcat7.noarch":
		ensure => "present",
		require => Package["java-1.7.0-openjdk.x86_64"]
	}

	service { "tomcat7":
		ensure => "running",
		require => Package["tomcat7.noarch"]
	}
}

class getNeon {
	package { "wget":
		ensure => "present"
	}

	exec { "wget http://neonframework.org/versions/latest/neon.war":
		cwd => "/var/lib/tomcat7/webapps",
		creates => "/var/lib/tomcat7/webapps/neon.war",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"],
		require => Package["wget", "tomcat7.noarch"]
	}

	exec { "wget http://neonframework.org/versions/latest/neon-examples.war":
		cwd => "/var/lib/tomcat7/webapps",
		creates => "/var/lib/tomcat7/webapps/neon-examples.war",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"],
		require => Package["wget", "tomcat7.noarch"]
	}
}

class restartTomcat {
	exec { "service tomcat7 restart":
		require => Class["tomcat7", "getNeon"]
	}
}

class getDemoData {
	package { "git":
		ensure => "present"
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
		command => "jar -xvf examples/angular-demo/data/earthquakes.zip",
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

include open8080, java7, mongodb, tomcat7, getNeon, restartTomcat, getDemoData, unzipDemoData, setupDemoData
