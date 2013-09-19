import java.io.*;
import java.util.*;


class MovieSort implements Comparable<MovieSort> {
    String movieID;
    double score; // P(X and Y) / P(X)

    public MovieSort(String mov, double sc) {
        movieID = mov;
        score = sc;
    }

    public int compareTo(MovieSort b) {
        return (int)(b.score*10000) - (int)(this.score*10000);
    }
}

class MovieAnalyze {

    // - P(!x)
    List<LineInput> inputList = new ArrayList<LineInput>();
    Double[][] ratings;
    Map<String, Integer> movieID;
    Map<String, Integer> userID;
    String movieX; // Reference movie
    int countX; // Count of users who rated movie X
    int countAll; // Count of users
    double probX; // CountX / CountAll
    Map<String, Double> probXandY = new HashMap<String, Double>();
    Map<String, Double> probNotXandY = new HashMap<String, Double>();
    double probNotX;
    MovieSort[] simpleScore;
    MovieSort[] advancedScore;

    // - P(X) = count of users who rated X / count of all distinct movies
    private double getProbX() {
        int count = 0;
        for (int i=0; i<inputList.size(); i++) {
            LineInput li = inputList.get(i);
            //Boolean c = map.get(li.userID);
            if (li.movieID.equals(movieX)) {
                count++;
            }
        }

        return (double)count/(double)userID.size();
    }

    // - P(X and Y) = count of users who rated X and Y / count of all distinct movies
    private void getProbXandY() {
        int count = 0;
        int movieXindex = movieID.get(movieX);

        //System.out.println("movieXindex = " + movieXindex);

        for (int i=0; i<movieID.size(); i++) {
            if (i != movieXindex) {
                count = 0;
                for (int j=0; j<userID.size(); j++) {
                    if ((ratings[j][movieXindex] != null) && (ratings[j][i] != null)) {
                        count++;
                    }
                    /*if ((ratings[j][i] != null)) {
                        count++;
                    } */
                }
                //System.out.println("movie[" + getMapKey(movieID,i) + "] count = " + count);
                double prob = (double)count/(double)userID.size();
                String mov = (String)getMapKey(movieID,i);
                probXandY.put(mov, prob);
            }
        }

        for (Map.Entry myentry : probXandY.entrySet()) {
            String mov = (String) myentry.getKey();
            Double prob = (Double) myentry.getValue();
            //System.out.println("Prob(Movie " + mov + " and Movie " + movieX + "; prob = " + prob);
        }
        //System.out.println("probXandY.size() = " + probXandY.size());

        return;
    }

    private double getProbNotX() {

        return 1-getProbX();
    }

    // - P(!x and y)
    private void getProbNotXandY() {
        int count = 0;
        int movieXindex = movieID.get(movieX);

        //System.out.println("movieXindex = " + movieXindex);

        for (int i=0; i<movieID.size(); i++) {
            if (i != movieXindex) {
                count = 0;
                for (int j=0; j<userID.size(); j++) {
                    if ((ratings[j][movieXindex] == null) && (ratings[j][i] != null)) {
                        count++;
                    }
                }
                //System.out.println("movie[" + getMapKey(movieID,i) + "] count = " + count);
                double prob = (double)count/(double)userID.size();
                String mov = (String)getMapKey(movieID,i);
                probNotXandY.put(mov, prob);
            }
        }

        for (Map.Entry myentry : probNotXandY.entrySet()) {
            String mov = (String) myentry.getKey();
            Double prob = (Double) myentry.getValue();
            //System.out.println("Prob(Movie " + mov + " and !Movie " + movieX + "; prob = " + prob);
        }
        //System.out.println("probNotXandY.size() = " + probNotXandY.size());

        return;
    }


    // Constructor that takes in matrix of movie ratings and reference movie
    // Calculate and sort by top 5
    //
    public MovieAnalyze(Double[][] ratingsmatrix, List<LineInput> l, Map<String, Integer> movie,
                        Map<String, Integer> user, String mov) {
        ratings = ratingsmatrix;
        inputList = l;
        movieID = movie;
        userID = user;
        movieX = mov;

        double probX = getProbX();
        getProbXandY();
        double probNotX = getProbNotX();
        //System.out.println("movieX = " + movieX);
        getProbNotXandY();

        // Calculate Simple
        simpleScore = new MovieSort[probXandY.size()];
        int i = 0;
        for (Map.Entry myentry : probXandY.entrySet()) {
            String movi = (String) myentry.getKey();
            double sco = (Double) myentry.getValue()/probX;
            simpleScore[i] = new MovieSort(movi, sco);
            i++;
        }
        Arrays.sort(simpleScore);


        // Calculate Advanced
    }

    // Return top 5 movie and probability in String
    public String getTop5Simple() {
        String a = "";

        System.out.printf("%s", movieX);
        for (int j=0; j<5; j++) {
            //System.out.println("Movie " + simpleScore[j].movieID + " = " + simpleScore[j].score);
            System.out.printf(",%s,%.2f", simpleScore[j].movieID, simpleScore[j].score);
        }
        System.out.printf("\n");

        return a;
    }

    // Return top 5 movie and probability in String
    public String getTop5Advanced() {
        String a = "";
        return a;
    }

    public static Object getMapKey(Map<String, Integer> map, Integer val) {
        for (Map.Entry myentry : map.entrySet()) {
            Integer compval = (Integer) myentry.getValue();

            if (compval.intValue() == val.intValue()) {
                //System.out.println(entry.getKey());
                return myentry.getKey();
            }
            //entry.getKey() + "," + entry.getValue());
        }
        return null;
    }
}


class LineInput {
    public String userID;
    public String movieID;
    public Double rating;
    //public String line;

    public LineInput(String a) {
        StringTokenizer st = new StringTokenizer(a, ",");
        if (st.hasMoreTokens()) {
            userID = st.nextToken();
        }

        if (st.hasMoreTokens()) {
            movieID = st.nextToken();
        }

        if (st.hasMoreTokens()) {
            rating = Double.parseDouble(st.nextToken());
        }

    }

}

class NonPersRecommender {

    private static Map<String, Integer> moviemap = new HashMap<String, Integer>();
    private static Map<String, Integer> usermap = new HashMap<String, Integer>();
    private static List<LineInput> inputList = new ArrayList<LineInput>();

    public static Integer getCountX(Double[][] matrix, String movieX) {
        int rowsize = matrix.length;
        int colsize = matrix[0].length;
        int count = 0;
        int movieXcolval = moviemap.get(movieX);

        //System.out.println("movieXcolval = " + movieXcolval);

        for (int i=0; i<rowsize; i++) {
            if (matrix[i][movieXcolval] != null)
                count++;
        }

        //System.out.println("count = " + count);

        return count;
    }

    public static Object getMapKey(Map<String, Integer> map, Integer val) {
        for (Map.Entry myentry : map.entrySet()) {
            Integer compval = (Integer) myentry.getValue();

            if (compval.intValue() == val.intValue()) {
                //System.out.println(entry.getKey());
                return myentry.getKey();
            }
            //entry.getKey() + "," + entry.getValue());
        }
        return null;
    }

    public static void main(String[] args) {

        //List<LineInput> inputList = new ArrayList<LineInput>();

        try {
            //FileOutputStream outStr = new FileOutputStream(file, true);
            BufferedReader in =
                    new BufferedReader(new FileReader("c:\\projects\\intro-recom\\pa1\\recsys_data_ratings.csv"));

            String text;

            try {

                // Read in file to array list
                while (in.ready()) {
                    text = in.readLine();
                    LineInput line = new LineInput(text);
                    inputList.add(line);
                    //System.out.println(text);
                }
                in.close();

                // Sanity check - output from array list
                /*Iterator<LineInput> iterator = inputList.iterator();
                while (iterator.hasNext()) {
                    LineInput li = iterator.next();
                    //li = iterator.next();
                    System.out.print(li.userID);
                    System.out.print(",");
                    System.out.print(li.movieID);
                    System.out.print(",");
                    System.out.println(li.rating);
                    //String a = iterator.next().line;
                    //System.out.println(a);
                }
                */
                //System.out.println(inputList.size());

                // Check distinct number of users and populate user lookup hashmap <k, v> = <userID, num>
                //Map<String, Integer> usermap = new HashMap<String, Integer>();
                Integer lsize = inputList.size();
                Integer mapindex = 0;
                for (int i = 0; i < lsize; i++) {
                    LineInput li = inputList.get(i);
                    //Boolean c = map.get(li.userID);
                    if (!usermap.containsKey(li.userID)) {
                        usermap.put(li.userID, mapindex++);
                        //if(li.userID == 5360)
                    }
                }
                /*for (Map.Entry entry : usermap.entrySet()) {
                    System.out.println("key,val: " + entry.getKey() + "," + entry.getValue());
                } */
                //System.out.println(usermap.size());

                // Check distinct number of movies and populate movie lookup dictionary <k, v> = <movieID, num>
                //Map<String, Integer> moviemap = new HashMap<String, Integer>();
                lsize = inputList.size();
                mapindex = 0;
                for (int i = 0; i < lsize; i++) {
                    LineInput li = inputList.get(i);
                    if (moviemap.get(li.movieID) == null) {
                        moviemap.put(li.movieID, mapindex++);
                    }
                }
                /*for (Map.Entry entry : moviemap.entrySet()) {
                    System.out.println("key,val: " + entry.getKey() + "," + entry.getValue());
                }
                System.out.println(moviemap.size());
                */

                // Create and populate ratings matrix
                Double[][] ratingsmatrix = new Double[usermap.size()][moviemap.size()];
                for (int i=0; i<usermap.size(); i++) {
                    for (int j=0; j<moviemap.size(); j++) {
                        ratingsmatrix[i][j] = null;
                    }
                }
                lsize = inputList.size();
                for (int i = 0; i < lsize; i++) {
                    LineInput li = inputList.get(i);
                    ratingsmatrix[usermap.get(li.userID)][moviemap.get(li.movieID)] = li.rating;
                }

                //System.out.println(usermap.size());
                for (int i=0; i<usermap.size(); i++) {
                    for (int j=0; j<moviemap.size(); j++) {
                        /*System.out.println("ratingsmatrix[" + getMapKey(usermap, i) + "][" +
                                getMapKey(moviemap, j) +
                                "] = " + ratingsmatrix[i][j]);
                        */
                        //System.out.println("ratingsmatrix[" + i + " " + getMapKey(usermap, i));
                    }
                }

                // Enumerate ratings matrix to calculate simple product association for the 3 movies (use Java HashMap)
                // X = 640, 275, 607
                // - P(X and Y)
                // - P(X) = count of X / count of all distinct movies
                int movieX = 640;
                int countX = getCountX(ratingsmatrix, "640");
                int countAll = usermap.size();
                double probX = (double)countX / (double)countAll;
                //System.out.println("countX = " + countX + "; countAll = " + countAll + "; probX = " + probX);
                //System.out.println(countX);

                MovieAnalyze movie640 = new MovieAnalyze(ratingsmatrix, inputList, moviemap, usermap, "640");
                String stringSimple640 = movie640.getTop5Simple();

                MovieAnalyze movie11 = new MovieAnalyze(ratingsmatrix, inputList, moviemap, usermap, "11");
                String stringSimple11 = movie11.getTop5Simple();

                // Enumerate ratings matrix to calculate advanced product association
                // - P(X and Y)
                // - P(X)
                // - P(!x and y)
                // - P(!x)



            } catch(IOException e) {
                System.out.println(e.getMessage());
            }

        } catch(FileNotFoundException fnfe) {
            System.out.println(fnfe.getMessage());
        }
        //BufferedReader in = new BufferedReader(new FileReader("c:\\projects\\intro-recom\\pa1\\recsys_data_ratings.csv"));

        // Movies array contains the movie IDs of the top 5 movies.
        int movies[] = new int[5];

        System.out.println("Hello world");

        // Write the top 5 movies, one per line, to a text file.
        try {
            PrintWriter writer = new PrintWriter("pa1-result.txt","UTF-8");

            for (int movieId : movies) {
                writer.println(movieId);
            }

            writer.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}