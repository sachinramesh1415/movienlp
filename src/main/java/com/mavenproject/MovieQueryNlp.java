package com.mavenproject;


import com.mavenproject.domain.Output;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieQueryNlp
{
    ArrayList<String> conceptList = new ArrayList<>();
    ArrayList<String> movieList = new ArrayList<>();
    ArrayList<String> boxOfficeList = new ArrayList<>();
    ArrayList<String> productionHouseList = new ArrayList<>();
    ArrayList<String> actorList = new ArrayList<>();
    ArrayList<String> producerList = new ArrayList<>();
    ArrayList<String> directorList = new ArrayList<>();
    ArrayList<String> releaseYearList = new ArrayList<>();
    ArrayList<String> countryList = new ArrayList<>();
    ArrayList<String> genreList = new ArrayList<>();
    ArrayList<String> awardsList = new ArrayList<>();
    ArrayList<String> nominationsList = new ArrayList<>();
    ArrayList<String> languageList = new ArrayList<>();
    public MovieQueryNlp(){
        conceptList.add("movie");
        conceptList.add("actor");
        conceptList.add("act");
        conceptList.add("direct");
        conceptList.add("box office collection");
        conceptList.add("production house");
        conceptList.add("produce");
        conceptList.add("release");
        conceptList.add("located");
        conceptList.add("genre");
        conceptList.add("award");
        conceptList.add("nominate");
        conceptList.add("language");
        movieList.add("Inception");
        movieList.add("Baahubali");
        boxOfficeList.add("$828.3 million");
        boxOfficeList.add("100cr");
        productionHouseList.add("Legendary Pictures");
        productionHouseList.add("Syncopy");
        actorList.add("Leonardo DiCaprio");
        actorList.add("Ken Watanabe");
        actorList.add("Joseph Gordon-Levitt");
        actorList.add("Shah Rukh Khan");
        actorList.add("Kajol");
        actorList.add("Shahid Kapoor");
        producerList.add("Emma Thomas");
        producerList.add("Salman Khan");
        directorList.add("Christopher Nolan");
        directorList.add("Karan Johar");
        directorList.add("Steven Spielberg");
        releaseYearList.add("2010");
        releaseYearList.add("2007");
        countryList.add("United States");
        countryList.add("United Kingdom");
        countryList.add("India");
        genreList.add("action");
        genreList.add("science fiction");
        awardsList.add("Academy Award for Best Cinematography");
        awardsList.add("Oscar Award");
        nominationsList.add("Academy Award for Best Picture");
        languageList.add("English");
    }
    private String[] stopWords = new String[]{"what","and","like","taken","also","for","it", "with","who","which","required","used","do","is","?", "by","take","are", "give", "times","me","How","many", "the","if","a","has","getting","that","do","name","after","before","between"};
    private String[] releaseWords = new String[]{"in","of"};
    private static Properties properties;
    private static String propertiesName = "tokenize, ssplit, pos, lemma";
    private static StanfordCoreNLP stanfordCoreNLP;
    static {
        properties = new Properties();
        properties.setProperty("annotators", propertiesName);
    }
    public static StanfordCoreNLP getPipeline() {
        if (stanfordCoreNLP == null) {
            stanfordCoreNLP = new StanfordCoreNLP(properties);
        }
        return stanfordCoreNLP;
    }
    public String getTrimmedQuery(String query) {
        String trimmedQuery = query.trim();
        trimmedQuery = trimmedQuery.replaceAll("\\s+", " ");
        trimmedQuery = trimmedQuery.replaceAll("\\t", " ");
        trimmedQuery = trimmedQuery.replaceAll("[?.!,]","");
        return trimmedQuery;
    }
    public ArrayList<String> getListWithoutStopWords(String query) {
        String trimmedQuery = getTrimmedQuery(query);
        String[] wordsSplitArray = trimmedQuery.split(" ");
        ArrayList<String> wordsSplitList = new ArrayList<String>();
        for (int i = 0; i < wordsSplitArray.length; i++) wordsSplitList.add(wordsSplitArray[i]);
        for (int i = 0; i < stopWords.length; i++) {
            for (int j = 0; j < wordsSplitList.size(); j++) {
                if (wordsSplitList.get(j).equalsIgnoreCase(stopWords[i].trim())) {
                    wordsSplitList.remove(wordsSplitList.get(j));
                }
            }
        }
        return wordsSplitList;
    }
    public List<String> getLemmatizedList(String query) {

        List<String> lemmatizedWordsList = new ArrayList<String>();
        ArrayList<String> listWithoutStopWords = getListWithoutStopWords(query);
        String stringWithoutStopWords = "";
        for (int i = 0; i < listWithoutStopWords.size(); i++) {
            stringWithoutStopWords = stringWithoutStopWords + listWithoutStopWords.get(i) + " ";
        }
        Annotation document = new Annotation(stringWithoutStopWords);
        StanfordCoreNLP stanfordCoreNLP = getPipeline();
        // run all Annotators on this text
        stanfordCoreNLP.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmatizedWordsList.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }
        return lemmatizedWordsList;
    }
    public Output RedisMatcher(String lemmatizedString)
    {
        Output output = new Output();
        output.setDomain("movie");

        int flag=0;
        String movie="";
        for(int i=0; i < movieList.size(); i++ ){
            Pattern pattern = Pattern.compile(movieList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                if(flag==0)
                {
                    lemmatizedString = lemmatizedString.replaceAll("movie", "");
                    movie = movieList.get(i);
                    lemmatizedString = lemmatizedString.replaceAll(movieList.get(i),"");
                    flag =1;
                }
                else {
                    if(movie.length()<movieList.get(i).length())
                    {
                        movie = movieList.get(i);
                        lemmatizedString = lemmatizedString.replaceAll(movieList.get(i),"");
                    }
                }
            }
        }
        if(flag==1)
        {
            output.setConstraint("movie", movie);
        }

        String productionHouse="";
        for(int i=0; i < productionHouseList.size(); i++ ){
            Pattern pattern = Pattern.compile(productionHouseList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find()){
                lemmatizedString = lemmatizedString.replaceAll("production house", "");
                productionHouse = productionHouse + ", " + productionHouseList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(productionHouseList.get(i),"");
            }
        }
        if(!(productionHouse.equals(""))) output.setConstraint("production house", productionHouse.substring(2,productionHouse.length()));

        String genre="";
        for(int i=0; i<genreList.size();i++)
        {
            Pattern pattern = Pattern.compile(genreList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("genre", "");
                genre = genre + ", " + genreList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(genreList.get(i),"");
            }
        }
        if(!(genre.equals(""))) output.setConstraint("genre", genre.substring(2,genre.length()));

        String actors="";
        for(int i=0; i<actorList.size();i++)
        {
            Pattern pattern = Pattern.compile(actorList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("act in", "");
                lemmatizedString = lemmatizedString.replaceAll("act", "");
                actors = actors + ", " + actorList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(actorList.get(i),"");
            }
        }
        if(!(actors.equals(""))) output.setConstraint("actor", actors.substring(2,actors.length()));

        String producers="";
        for(int i=0; i<producerList.size();i++)
        {
            Pattern pattern = Pattern.compile(producerList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("produce", "");
                producers = producers + ", " + producerList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(producerList.get(i),"");
            }
        }
        if(!(producers.equals(""))) output.setConstraint("producer", producers.substring(2,producers.length()));

        String directors="";
        for(int i=0; i<directorList.size();i++)
        {
            Pattern pattern = Pattern.compile(directorList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("direct", "");
                directors = directors + ", " + directorList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(directorList.get(i),"");
            }
        }
        if(!(directors.equals(""))) output.setConstraint("director", directors.substring(2,directors.length()));

        String country="";
        for(int i=0; i<countryList.size();i++)
        {
            Pattern pattern = Pattern.compile(countryList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("located", "");
                lemmatizedString = lemmatizedString.replaceAll("set", "");
                country = country + ", " + countryList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(countryList.get(i),"");
            }
        }
        if(!(country.equals(""))) output.setConstraint("country", country.substring(2,country.length()));

        String awards="";
        for(int i=0; i<awardsList.size();i++)
        {
            Pattern pattern = Pattern.compile(awardsList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("award", "");
                awards = awards + ", " + awardsList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(awardsList.get(i),"");
            }
        }
        if(!(awards.equals(""))) output.setConstraint("award", awards.substring(2,awards.length()));

        String nominations="";
        for(int i=0; i<nominationsList.size();i++)
        {
            Pattern pattern = Pattern.compile(nominationsList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("nominate", "");
                nominations = nominations + ", " + nominationsList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(nominationsList.get(i),"");
            }
        }
        if(!(nominations.equals(""))) output.setConstraint("nominations", nominations.substring(2,nominations.length()));

        String language="";
        for(int i=0; i<languageList.size();i++)
        {
            Pattern pattern = Pattern.compile(languageList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                lemmatizedString = lemmatizedString.replaceAll("language", "");
                language = language + ", " + languageList.get(i);
                lemmatizedString = lemmatizedString.replaceAll(languageList.get(i),"");
            }
        }
        if(!(language.equals(""))) output.setConstraint("language", language.substring(2,language.length()));

        String releaseYear="";
        int flag1=0;
        Pattern pattern1 = Pattern.compile("[1-9][0-9]{3}");
        Matcher matcher1 = pattern1.matcher(lemmatizedString.trim());
        for(int i=0; i < releaseWords.length; i++ ){
            if(lemmatizedString.contains(releaseWords[i])){
                lemmatizedString = lemmatizedString.replaceAll("release", "");
                releaseYear = releaseYear + " " + releaseWords[i];
                lemmatizedString = lemmatizedString.replaceAll(releaseWords[i],"");
                while(matcher1.find())
                {
                    flag1=1;
                    releaseYear = releaseYear + " " + matcher1.group();
                    lemmatizedString = lemmatizedString.replaceAll(matcher1.group(),"");
                }
            }
        }
        if(!(releaseYear.equals(""))&&flag1==1) output.setConstraint("release year", releaseYear.trim());

        for(int i=0; i < boxOfficeList.size(); i++ ){
            Pattern pattern = Pattern.compile(boxOfficeList.get(i));
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find()){
                lemmatizedString = lemmatizedString.replaceAll("box office collection", "");
                lemmatizedString = lemmatizedString.replaceAll("collect", "");
                lemmatizedString = lemmatizedString.replaceAll("gross", "");
                output.setConstraint("box office collection", boxOfficeList.get(i));
                lemmatizedString = lemmatizedString.replaceAll(boxOfficeList.get(i),"");
            }
        }

        String concepts="";
        for (int i=0; i<conceptList.size();i++)
        {
            lemmatizedString = lemmatizedString.replaceAll("film", "movie");
            Pattern pattern = Pattern.compile(conceptList.get(i).toLowerCase());
            Matcher matcher = pattern.matcher(lemmatizedString);
            if(matcher.find())
            {
                if(conceptList.get(i).equals("act")||conceptList.get(i).equals("actor"))
                {
                    concepts = concepts + ", " + "actor";
                    lemmatizedString = lemmatizedString.replaceAll("act", "");
                    lemmatizedString = lemmatizedString.replaceAll("actor","");
                }
                else {
                    concepts = concepts + ", " + conceptList.get(i);
                }
            }
        }
        if(!(concepts.equals(""))) output.setQueryResult(concepts.substring(2,concepts.length()));
        return output;
    }
}

