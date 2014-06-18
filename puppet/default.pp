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
		ensure => "present"
	}

	service { "tomcat7":
		ensure => "running",
		require => Package["tomcat7.noarch"]
	}
}

class getNeon {
	exec { "wget http://neonframework.org/versions/latest/neon.war":
		cwd => "/var/lib/tomcat7/webapps",
		creates => "/var/lib/tomcat7/webapps/neon.war",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"],
		require => Package["tomcat7.noarch"]
	}

	exec { "wget http://neonframework.org/versions/latest/neon-examples.war":
		cwd => "/var/lib/tomcat7/webapps",
		creates => "/var/lib/tomcat7/webapps/neon-examples.war",
		path => ["/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/"],
		require => Package["tomcat7.noarch"]
	}
}

include open8080, java7, mongodb, tomcat7, getNeon
