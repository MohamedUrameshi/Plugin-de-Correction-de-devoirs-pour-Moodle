package fr.up5.miage.utility;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import fr.up5.miage.configuration.ModelValue;
import fr.up5.miage.sonarqube.SonarDataBase;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;
import fr.up5.miage.testsReport.TestRepport;

/**
 * This class provides calculation methods for rules and metrics grading
 */
public class UtilCalculGrade{


	/**
	 * Calculate the lost points for a "superior" metric like comment_lines_density metric
	 * @param value represents the configuration in the notation configuration for the quality axis that contains this metric
	 * @param scoreMetricTeacher is the result of the metric for the teacher project
	 * @param scoreMetricStudent is the result of the metric for the student project
	 * @return a Float that represents the number of lost points
	 */
	public static Float calculMetricSuperior(ModelValue value, Float scoreMetricTeacher, Float scoreMetricStudent){
		
		if(scoreMetricStudent ==  null)  scoreMetricStudent = 0f;
		if (scoreMetricStudent < ((1-(value.getFirstValue()/100)))*scoreMetricTeacher){
			return value.getSecondValue();
		}
		else{
			return 0f;
		}
	}


	/**
	 * Calculate the lost points for an "inferior" metric like complexity metric
	 * @param value represents the configuration in the notation configuration for the quality axis that contains this metric
	 * @param scoreMetricTeacher scoreMetricTeacher is the result of the metric for the teacher project
	 * @param scoreMetricStudent scoreMetricStudent is the result of the metric for the student project
	 * @return a Float that represents the number of lost points
	 */
	public static Float calculMetricInferior(ModelValue value, Float scoreMetricTeacher, Float scoreMetricStudent){
		if(scoreMetricStudent ==  null)  scoreMetricStudent = 1f;
		if (scoreMetricStudent > ((value.getFirstValue()/100))*scoreMetricTeacher+scoreMetricTeacher){
			return value.getSecondValue();
		}
		else{
			return 0f;
		}
	}


	/**
	 * Calculate the lost points for the TestOfStudent quality axe
	 * @param value represents the configuration in the notation configuration for this axe quality
	 * @param scoreStudent is the number of functional unit tests
	 * @return a Float that represents the number of lost points
	 */
	public static Float calculTestOfStudentQualityAxis(ModelValue value, Float scoreStudent){
		if (scoreStudent < value.getFirstValue()){
			return value.getSecondValue();
		}
		else return 0f;
	}


	/**
	 * Calculate the lost points for one not respected rule
	 * @param value represents the configuration in the notation configuration file for this rule
	 * @param numberOfNotRespected is the number of times that the rule is not respected
	 * @return a Float that represents the number of lost points
	 */
	public static Float calculLostPointsByOneRule(ModelValue value, Integer numberOfNotRespected){
		System.out.println("value : " + value + "numberOfNotRespected : " + numberOfNotRespected);
		Float tmp = 0f;
		
		if(value != null) {
		Float mini = value.getFirstValue();
		Float lostPoints = value.getSecondValue();
		Float maxi = value.getThirdValue();
		
		//System.out.println("NDIONE  POIIIINTTTTTTTTTTTTTTTT");
		
		
		System.err.println("mini "+mini);
		System.err.println("lost "+mini);
		System.err.println("max  "+mini);
		//System.out.println("End     NDIONE  POIIIINTTTTTTTTTTTTTTTT");
		
		if (numberOfNotRespected >= maxi){
			return maxi*lostPoints;
		}
		else if (numberOfNotRespected >= mini){
			return numberOfNotRespected*lostPoints;
		}
		else{
			tmp = 0f;
		}
			
		}
		return tmp;
		
	}
	public static Float calculLostPointsByTests(String projectName,int idModule) throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException 
	{
		
		int projectID = SonarDataBase.getInstance().getProjectID(projectName);
		System.err.println("porject id "+projectID);
		System.err.println("project name"+projectName);
		float result  = SonarDataBase.getInstance().getTestScore(projectID,idModule);
		return result;
	}
	/**
	 * This method calculates the whole lost points by quality axis
	 * @param mapQualityAxis a Map of the name of quality and theirs values
	 * @param mapScoreMetricsStudent stocks all the score of metrics for a student project
	 * @param mapScoreMetricsTeacher stocks all the score of metrics for a teacher project
	 * @return the number of lost points
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws SonarqubeDataBaseException 
	 * @throws ClassNotFoundException 
	 */
	public static HashMap<String,Float> calculLostPointsByQualityAxis(HashMap<String,ModelValue> mapQualityAxis, HashMap<String,Float> mapScoreMetricsStudent, HashMap<String, Float> mapScoreMetricsTeacher,String ProjectName,int idModule) throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException{
		System.out.println();
		System.out.println();
		System.out.println();
		
		System.out.println("Depuis calculLostPointsByQualityAxis ");
		
		for(String i :mapScoreMetricsStudent.keySet()) 
		{
			System.err.println("keyssssStudent :"+i+ "  valuesss :"+mapScoreMetricsStudent.get(i));
		}
		
		for(String i :mapScoreMetricsTeacher.keySet()) 
		{
			System.err.println("keyssssTeacher :"+i+ "  valuesss :"+mapScoreMetricsTeacher.get(i));
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		HashMap<String,Float> lostPointsByQualityAxis = new HashMap<String,Float>();
		for (String qualityAxis : mapQualityAxis.keySet()){
			switch (qualityAxis){
			//case "TestOfTeacher": lostPointsByQualityAxis.put("TestOfTeacher",20*(1-mapScoreMetricsStudent.get("test_success_density_teacher")));break;
			case "TestOfTeacher": lostPointsByQualityAxis.put("TestOfTeacher",calculLostPointsByTests(ProjectName,idModule));break;

			// à delete
			case "TestOfStudent":{
				try{
					lostPointsByQualityAxis.put("TestOfStudent",UtilCalculGrade.calculTestOfStudentQualityAxis(mapQualityAxis.get(qualityAxis), mapScoreMetricsStudent.get("tests")*(mapScoreMetricsStudent.get("test_success_density")/100)));
				}
				catch (NullPointerException e){
					lostPointsByQualityAxis.put("TestOfStudent",mapQualityAxis.get(qualityAxis).getSecondValue());
				}
			};break;
			case "Complexity": lostPointsByQualityAxis.put("Complexity",UtilCalculGrade.calculMetricInferior(mapQualityAxis.get(qualityAxis),mapScoreMetricsTeacher.get("complexity"),mapScoreMetricsStudent.get("complexity")));break;
			default: lostPointsByQualityAxis.put("Comments", UtilCalculGrade.calculMetricSuperior(mapQualityAxis.get(qualityAxis), mapScoreMetricsTeacher.get("comment_lines_density"), mapScoreMetricsStudent.get("comment_lines_density")));
			}
			
		}
		return lostPointsByQualityAxis;
	}
	


	/**
	 * This method calculates the whole lost points by rules
	 * @param rulesAndValues a Map of the rules id and theirs values
	 * @param issuesProject a Map of the not respected rules id and the number of times that each rule is not respected
	 * @return a Float that represents the number of lost points
	 */
	public static Float calculLostPointsByRules(HashMap<String,ModelValue> rulesAndValues, HashMap<String,Integer> issuesProject){
		Float lostPoints = 0f;
		for (String idRule : issuesProject.keySet()){
			lostPoints += UtilCalculGrade.calculLostPointsByOneRule(rulesAndValues.get(idRule), issuesProject.get(idRule));
			System.err.println(lostPoints);
		}
		System.out.println();
		
		System.err.println("points totals perdus pour les rules "+ lostPoints);
		return lostPoints;
	}


	/**
	 * This method returns all results about all quality axis and rules and calculates the final grade
	 * @param mapQualityAxis is a Map that contains the name of quality and theirs values
	 * @param mapScoreMetricsStudent is a Map that stocks all the scores of metrics for a student project
	 * @param mapScoreMetricsTeacher is a Map that stocks all the scores of metrics for a teacher project
	 * @param rulesAndValues a Map of the rules id and theirs values
	 * @param issuesProject a Map of the not respected rules id and the number of times that each rule is not respected
	 * @return all lost points for each quality axis and rules and returns the final grade in a HashMap
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws SonarqubeDataBaseException 
	 * @throws ClassNotFoundException 
	 */
	public static HashMap<String,Float> getAllGrades(HashMap<String,ModelValue> mapQualityAxis, HashMap<String,Float> mapScoreMetricsStudent, HashMap<String,Float> mapScoreMetricsTeacher, HashMap<String,ModelValue> rulesAndValues, HashMap<String,Integer> issuesProject,String projectName,int idModule) throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException{
		HashMap<String,Float> allLostPoints = calculLostPointsByQualityAxis(mapQualityAxis, mapScoreMetricsStudent, mapScoreMetricsTeacher,projectName,idModule);
		System.err.println("rulesAndValues : " + rulesAndValues + "issuesProject : " + issuesProject);
		
		System.out.println("****** Rules chargés *****");
		for(String i : rulesAndValues.keySet()) 
		{
			System.err.println("key "+ i +"value" + rulesAndValues.get(i));
		}
		
		
		allLostPoints.put("Rules", calculLostPointsByRules(rulesAndValues, issuesProject));
		
		System.out.println();
		
		for(String i : allLostPoints.keySet()) 
		{
			System.err.println("key "+ i +"value" + allLostPoints.get(i));
		}
		
		Float finalGrade = 20f;
		Float minimalGrade;
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		int i=0;
		for (String catLostPoint : allLostPoints.keySet())
		{
			System.out.println("finalgrade :"+i+" ="+finalGrade+ "point :"+i+" perdu ="+allLostPoints.get(catLostPoint));
			finalGrade -= allLostPoints.get(catLostPoint);
			
		}
		try{
			if (finalGrade < (minimalGrade = (20-allLostPoints.get("TestOfTeacher"))*(1-(mapQualityAxis.get("TestOfTeacher").getFirstValue()/100)))){
				finalGrade = new Float(minimalGrade.toString().substring(0,3));
			}
		}
	
		catch (NullPointerException e)
		{
			System.out.println("Error getAllGrades "+e.getMessage());
		};
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
		allLostPoints.put("FinalGrade", finalGrade);
		return allLostPoints;
	}
}