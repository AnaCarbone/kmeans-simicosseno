import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	/**
	 *  Par�metros
	 * args[0] = nome do arquivo
	 * args[1] = k grupos
	 * args[2] = taxa de erro
	 * args[3] = tipo: binario, tf ou tfidf
	 * args[4] = linhas = documentos
	 * args[5] = colunas = dimens�es
	 * @param args
	 */
	public static void main(String [] args){
		boolean bomResultado = false; // controla o fato de algoritmo j� ter alcan�ado um bom resultado ou n�o
		int iteracoes = 0;
		double jcmAtual = 0;
		int[][] prototiposAnteriorInt;
		double[][] prototiposAnteriorDouble;
		double condicaoParada = 0;
		
		//Teste
		int[][] resp = null;
		
		if (args[3].equals("binario") || args[3].equals("tf")) {
			KMeans kMeans = new KMeans(args[0], Integer.parseInt(args[1]),
					Integer.parseInt(args[4]), Integer.parseInt(args[5]));
			
			prototiposAnteriorInt = kMeans.inicializarPrototipos();
			prototiposAnteriorInt = copiarPrototiposInt(prototiposAnteriorInt, Integer.parseInt(args[1]),
					Integer.parseInt(args[5]));
			
			while(!bomResultado){
				iteracoes++;
				kMeans.definirSimilaridadeCosseno();
				kMeans.clustering();
				jcmAtual = kMeans.calcularJCM();
				System.out.println("JCM: " + jcmAtual);
				kMeans.redefinirPrototipos();
				
				condicaoParada = kMeans.diferencaPrototipos(prototiposAnteriorInt);
				if (condicaoParada < Double.parseDouble(args[2])) {
					bomResultado = true;
					//Teste
					resp = kMeans.getMatrizParticao();
				}
				prototiposAnteriorInt = kMeans.getPrototipos();
				prototiposAnteriorInt = copiarPrototiposInt(prototiposAnteriorInt, Integer.parseInt(args[1]),
						Integer.parseInt(args[5]));
			}
			
	
		} else{
			/*
			 *  Se a representa��o do pr�-processamento for tfifd 
			 *  representaremos seus campos como double e n�o mais
			 *  int
			 */
			KMeansDouble kMeans = new KMeansDouble(args[0], Integer.parseInt(args[1]),
					Integer.parseInt(args[4]), Integer.parseInt(args[5]));
			
			prototiposAnteriorDouble = kMeans.inicializarPrototipos();
			prototiposAnteriorDouble = copiarPrototiposDouble(prototiposAnteriorDouble, Integer.parseInt(args[1]),
					Integer.parseInt(args[5]));
			
			while(!bomResultado){
				iteracoes++;
				kMeans.definirSimilaridadeCosseno();
				kMeans.clustering();
				jcmAtual = kMeans.calcularJCM();
				System.out.println("JCM: " + jcmAtual);
				kMeans.redefinirPrototipos();
				
				condicaoParada = kMeans.diferencaPrototipos(prototiposAnteriorDouble);
				if (condicaoParada < Double.parseDouble(args[2])) {
					bomResultado = true;
					//Teste
					resp = kMeans.getMatrizParticao();
				}
				prototiposAnteriorDouble = kMeans.getPrototipos();
				prototiposAnteriorDouble = copiarPrototiposDouble(prototiposAnteriorDouble, Integer.parseInt(args[1]),
						Integer.parseInt(args[5]));
				
			}
			
			
		}
		
		// TESTE printar grupos formados - matriz de particao
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter("resp.csv"));
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
				for (int j = 0; j < Integer.parseInt(args[4]); j++) {
					br.append(resp[i][j] + ",");
				}
				br.newLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static int[][] copiarPrototiposInt(int[][] prototipos, int linhas, int colunas) {
		int[][] prototiposAnterior = new int[linhas][colunas];
		for (int i = 0; i < linhas; i++) {
			for (int j = 0; j < colunas; j++) {
				prototiposAnterior[i][j] = prototipos[i][j];
			}
		}
		return prototiposAnterior;
	}
	
	public static double[][] copiarPrototiposDouble(double[][] prototipos, int linhas, int colunas) {
		double[][] prototiposAnterior = new double[linhas][colunas];
		for (int i = 0; i < linhas; i++) {
			for (int j = 0; j < colunas; j++) {
				prototiposAnterior[i][j] = prototipos[i][j];
			}
		}
		return prototiposAnterior;
	}
}
