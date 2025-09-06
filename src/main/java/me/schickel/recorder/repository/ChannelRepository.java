package me.schickel.recorder.repository;

import me.schickel.recorder.entity.ChannelUrl;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ChannelRepository extends CrudRepository<ChannelUrl, Long> {
    boolean existsByName(String name);
    Optional<ChannelUrl> findByName(String name);
}
