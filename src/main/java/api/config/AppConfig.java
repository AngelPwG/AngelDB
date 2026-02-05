package api.config;

import btree.BPlusTree;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import storage.BufferPool;
import storage.DiskManager;

@Configuration
public class AppConfig {
    @Bean
    public BPlusTree bPlusTree(){
        return new BPlusTree(39, new BufferPool(10, new DiskManager("angel.db")));
    }
}
