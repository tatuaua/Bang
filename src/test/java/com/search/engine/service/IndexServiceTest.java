package com.search.engine.service;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentCaptor.forClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import com.search.engine.model.Word;
import com.search.engine.repository.DatabaseRepository;
import static com.search.engine.util.Constants.STOP_WORDS;

public class IndexServiceTest {

    @Mock
    private DatabaseRepository databaseRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private IndexService indexService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateIndex() throws IOException {
        String content = "This is a test document with some test words";
        String fileName = "testDocument.txt";

        Word word1 = new Word();
        word1.setWord("test");
        word1.addPageOccurrence(fileName);
        word1.updatePageOccurrence(fileName);

        Word word2 = new Word();
        word2.setWord("words");
        word2.addPageOccurrence(fileName);

        when(multipartFile.getBytes()).thenReturn(content.getBytes(StandardCharsets.UTF_8));
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Word>> captor = forClass(List.class);

        indexService.updateIndex(multipartFile);

        verify(databaseRepository).upsertIndex(captor.capture());

        List<Word> capturedArgument = captor.getValue();

        assert (capturedArgument.contains(word1));
        assert (capturedArgument.contains(word2));
    }

    @Test
    public void testUpdateIndexWithStopWords() throws IOException {

        String stopWord = STOP_WORDS.toArray()[0].toString();
        String content = String.format("{} string is a test document", stopWord);
        String fileName = "stopWordsDocument.txt";

        Word wordThatShouldNotExist = new Word();
        wordThatShouldNotExist.setWord(stopWord);
        wordThatShouldNotExist.addPageOccurrence(fileName);

        Word wordThatShouldExist = new Word();
        wordThatShouldExist.setWord(content.split(" ")[1]);
        wordThatShouldExist.addPageOccurrence(fileName);

        when(multipartFile.getBytes()).thenReturn(content.getBytes(StandardCharsets.UTF_8));
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Word>> captor = forClass(List.class);

        indexService.updateIndex(multipartFile);

        verify(databaseRepository).upsertIndex(captor.capture());

        List<Word> capturedArgument = captor.getValue();

        assert (!capturedArgument.contains(wordThatShouldNotExist));
        assert (capturedArgument.contains(wordThatShouldExist));
        assert (capturedArgument.size() == 3);
    }
}
