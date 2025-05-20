package com.rookies3.myspringbootlab.service;

import com.rookies3.myspringbootlab.controller.dto.PublisherDTO;
import com.rookies3.myspringbootlab.entity.Publisher;
import com.rookies3.myspringbootlab.exception.BusinessException;
import com.rookies3.myspringbootlab.exception.ErrorCode;
import com.rookies3.myspringbootlab.repository.BookRepository;
import com.rookies3.myspringbootlab.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    public List<PublisherDTO.SimpleResponse> getAllPublishers(){
        List<Publisher> publishers = publisherRepository.findAll();

        return publishers.stream()
                .map(publisher -> {
                    Long bookCount = bookRepository.countByPublisherId(publisher.getId());
                    return PublisherDTO.SimpleResponse.builder()
                            .id(publisher.getId())
                            .name(publisher.getName())
                            .bookCount(bookCount)
                            .build();
                })
                .toList();
    }

    public PublisherDTO.Response getPublisherById(Long id){
        Publisher publisher = publisherRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", id));
        return PublisherDTO.Response.fromEntity(publisher);
    }

    public PublisherDTO.Response getPublisherByName(String name){
        Publisher publisher = publisherRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "name", name));
        return PublisherDTO.Response.fromEntity(publisher);
    }

    @Transactional
    public PublisherDTO.Response createPublisher(PublisherDTO.Request request){
        if (publisherRepository.existsByName(request.getName())){
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE,
                    request.getName());
        }

        Publisher publisher = Publisher.builder()
                .name(request.getName())
                .address(request.getAddress())
                .establishedDate(request.getEstablishedDate())
                .build();

        Publisher savedPublisher = publisherRepository.save(publisher);
        return PublisherDTO.Response.fromEntity(savedPublisher);
    }

    @Transactional
    public PublisherDTO.Response updatePublisher(Long id, PublisherDTO.Request request){
        Publisher publisher = publisherRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", id));

        if(!publisher.getName().equals(request.getName()) &&
                publisherRepository.existsByName(request.getName())){
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE,
                    request.getName());
        }

        publisher.setName(request.getName());
        publisher.setAddress(request.getAddress());
        publisher.setEstablishedDate(request.getEstablishedDate());

        Publisher updatedPublisher = publisherRepository.save(publisher);
        return PublisherDTO.Response.fromEntity(updatedPublisher);
    }

    @Transactional
    public void deletePublisher(Long id){
        if(!publisherRepository.existsById(id)){
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Publisher", "id", id);
        }

        Long bookCount = bookRepository.countByPublisherId(id);
        if(bookCount > 0){
            throw new BusinessException(ErrorCode.PUBLISHER_HAS_BOOKS,
                    id, bookCount);
        }

        publisherRepository.deleteById(id);
    }
}
