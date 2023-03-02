package com.webapp.cloudapp.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webapp.cloudapp.Entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

	List<Image> findAllByProductId(int parseInt);

}
