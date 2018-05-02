import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class KMeans {

	/**
	 * matriz de dados que o k-means receberá, cada linha um documento, cada
	 * coluna uma palavra
	 */
	private int[][] dados;

	/**
	 * quantidade k de prototipos passada
	 */
	private int k;
	
	/**
	 * matriz com os protótipos int[k][numeroDimensoes];
	 */
	private int[][] prototipos;
	
	/**
	 * matriz com a similaridade cosseno entre os documentos e os protótipos
	 */
	private double[][] similaridadeCosseno;

	/**
	 * matriz binaria de partição int[k][numeroLinhas]
	 */
	private int[][] matrizParticao;
	
	/**
	 * quantização do erro de agrupamento
	 */
	private double jcm;

	/**
	 * equivale ao numero de documentos
	 */
	private int numeroLinhas;
	
	/**
	 * equivale ao numero de palavras analisadas nos documentos
	 */
	private int numeroDimensoes;

	/**
	 * valores máximos dos números que podem ser escolhidos aleatoriamente para a
	 * criação dos prototipos
	 */
	private int[] max;


	public KMeans(String arquivo, int k, int linhas, int colunas) {
		try {
			BufferedReader leitor = new BufferedReader(new FileReader(arquivo));

			/*
			 * Para popular a matriz dados é necessário saber o número de linhas
			 * e o número de dimensões do corpus recebido
			 */
			numeroLinhas = linhas;
			numeroDimensoes = colunas;
			jcm = 0;
			this.k = k;


			/*
			 * Inicialização das matrizes
			 */
			dados = new int[numeroLinhas][numeroDimensoes];
			prototipos = new int[k][numeroDimensoes];
			similaridadeCosseno = new double[numeroLinhas][k];
			matrizParticao = new int[k][numeroLinhas];

			/*
			 *  Leitura do arquivo e população da matriz de dados
			 */
			max = new int[numeroDimensoes];
			String linha = null;
			String[] numColunas = null;
			int i = 0;
			int j = 0;
			/*
			 * Lê a linha que contém o nome das palavras
			 */
			leitor.readLine();
			
			
			while ((linha = leitor.readLine()) != null) {
				numColunas = linha.split(",");
				for(int co = 1; co <= colunas; co++) {
					dados[i][j] = Integer.parseInt(numColunas[co]);
					/*
					 * Os valores máximos já serão descobertos simultaneamente a população
					 */
					if (dados[i][j] > max[j]) {
						max[j] = dados[i][j];
					}
					j++;
				}
				j = 0;
				i++;
			}
			
			leitor.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * inicializa aleatoriamente os prototipos na primeira iteração
	 * considerando o valor máximo de cada dimensão
	 * @return
	 */
	public int[][] inicializarPrototipos() {
		int randomNumber = 0;
		Random rand = new Random();
		for(int prototipo = 0; prototipo < k; prototipo++){
			for(int palavra = 0; palavra < numeroDimensoes; palavra++){
				randomNumber = rand.nextInt(max[palavra] + 1);
				System.out.println(randomNumber + "  " + max[palavra]);
				prototipos[prototipo][palavra] = randomNumber;
			}
		}
		return prototipos;
	}
	
	/**
	 *  Define a similaridade cosseno entre todos os documentos com todos os prototipos
	 */
	public void definirSimilaridadeCosseno()
	{
		int atual = 0;
		double similaridade = 0;
		double somaXY = 0;
		double somaX = 0;
		double somaY = 0;
		
		/*
		 *  percorrendo prototipos
		 */
		for(int j = 0; j< k ; j++)
		{
			/*
			 * percorrendo documentos
			 * todos para cada prototipo
			 */
			for(int i = 0; i< numeroLinhas; i++)
			{
				/*
				 *  calculo da distancia para um documento
				 */
				while(atual<numeroDimensoes)
				{
					somaXY = somaXY + dados[i][atual] + prototipos[j][atual];
					somaX = somaX + (dados[i][atual]*dados[i][atual]);
					somaY = somaY + (prototipos[j][atual]*prototipos[j][atual]);
					atual++;
				}
				
				similaridade = somaXY/(Math.sqrt(somaX)*Math.sqrt(somaY));
				/*
				 * O valor de similaridade é retirado de 1 para o metódo de 
				 * agrupamento conseguir continuar considerando os maiores valores
				 * como mais distantes
				 */
				similaridadeCosseno[i][j] = 1 - similaridade;
				
				/*
				 *  atualizar todas as variáveis para o próximo vetor que será comparado com o protótipo atual
				 */
				atual = 0;
				somaXY = 0;
				somaX = 0;
				somaY = 0;
			}
		}
	}
	
	public void clustering() {
		inicializarMatrizParticao();
		double menorDistancia = 0;
		double atual = 0;
		int cluster = 0;
		for (int documento = 0; documento < numeroLinhas; documento++) {
			/*
			 *  Calcula qual o prototipo com menor distancia e assim define um
			 *  cluster para o dado
			 */
			for (int prototipo = 0; prototipo < k; prototipo++) {
				atual = similaridadeCosseno[documento][prototipo];
				if (prototipo == 0) {
					menorDistancia = atual;
					cluster = prototipo;
				}
				if (atual < menorDistancia) {
					menorDistancia = atual;
					cluster = prototipo;
				}
			}
			/*
			 *  adiciona o valor um a matriz de particao no local marcando o
			 *  cluster a qual pertence
			 */
			matrizParticao[cluster][documento] = 1;
		}

	}
	
	/**
	 * zera matriz de partição
	 */
	private void inicializarMatrizParticao() {

		for (int i = 0; i < k; i++) {
			for (int j = 0; j < numeroLinhas; j++) {
				matrizParticao[i][j] = 0;
			}
		}
	}

	public double calcularJCM() {
		double jcmAtual = 0;
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {

				jcmAtual = jcmAtual
						+ (matrizParticao[prototipo][documento] * (similaridadeCosseno[documento][prototipo] * similaridadeCosseno[documento][prototipo]));
			}
		}
		return jcmAtual;
	}
	
	public int [][] redefinirPrototipos() {
		int integrantes = 0; 
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {
				if (matrizParticao[prototipo][documento] == 1) {
					if (integrantes == 0){
						inicializarPrototipo(prototipo);
					}
					integrantes++;
					for(int palavra = 0; palavra < numeroDimensoes; palavra++){
						prototipos[prototipo][palavra] = prototipos[prototipo][palavra] + dados[documento][palavra];
					}
					
				}
				
			}
			/*
			 *  evitar erro aritimético de divisão por 0				
			 */
			if(integrantes>0){
				for (int palavra = 0; palavra < numeroDimensoes; palavra++) {
					prototipos[prototipo][palavra] = (prototipos[prototipo][palavra]/integrantes);
				}
			}
			integrantes = 0;
		}
		return prototipos;
	}

	/** 
	 * zera matriz de prototipos
	 * @param prototipo
	 */
	private void inicializarPrototipo(int prototipo) {
		for (int j = 0; j < numeroDimensoes; j++) {
			prototipos[prototipo][j] = 0;
		}
	}
	
	/** 
	 * similaridade cosseno entre os prototipos da iteração anterior com
	 * os da iteração atual para saber a movimentação que ocorreu
	 * @param prototiposAnterior
	 * @return
	 */
	public double diferencaPrototipos(int [][] prototiposAnterior) {
		double resposta = 0;
		double similaridade = 0;
		double somaXY = 0;
		double somaX = 0;
		double somaY = 0;
		for(int prototipo = 0; prototipo<k; prototipo++){
			for(int palavra = 0; palavra<numeroDimensoes; palavra++){
				somaXY = somaXY + prototipos[prototipo][palavra] + prototiposAnterior[prototipo][palavra];
				somaX = somaX + (prototipos[prototipo][palavra]*prototipos[prototipo][palavra]);
				somaY = somaY + (prototiposAnterior[prototipo][palavra]*prototiposAnterior[prototipo][palavra]);
			}
			resposta = resposta + (1 - (somaXY/(Math.sqrt(somaX)*Math.sqrt(somaY))));
			
			/*
			 * atualizar variáveis para o próximo protótipo
			 */
			somaXY = 0;
			somaX = 0;
			somaY = 0;
		}
		resposta = resposta/k;
		return resposta;
	}


	public int[][] getPrototipos() {
		return this.prototipos;
	}


	public int[][] getMatrizParticao() {
		return matrizParticao;
	}


	
	
	
}
