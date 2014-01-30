package com.ncc.neon

import org.junit.Test




class NeonPropertiesLoaderTest {

    @Test
    void "get owf url"(){
        NeonPropertiesLoader loader = new NeonPropertiesLoader()
        assert loader.owfUrl == "https://fakeOwfUrl"
    }

    @Test
    void "get neon url"(){
        NeonPropertiesLoader loader = new NeonPropertiesLoader()
        assert loader.neonUrl == "https://fakeNeonUrl"
    }

    @Test
    void "make sure properties default works"(){
        Properties props = new Properties();
        assert props.getProperty("something", "default") == "default"
    }
}
