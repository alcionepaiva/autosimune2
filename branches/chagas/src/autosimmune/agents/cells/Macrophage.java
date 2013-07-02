package autosimmune.agents.cells;

import java.util.ArrayList;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.engine.schedule.ScheduledMethod;
import autosimmune.agents.Antibody;
import autosimmune.agents.pathogens.TCruzi;
import autosimmune.agents.pathogens.Virus;
import autosimmune.defs.CitokineNames;
import autosimmune.defs.EnvParameters;
import autosimmune.defs.MacrophageStates;
import autosimmune.defs.PAMPS;
import autosimmune.defs.PCStates;
import autosimmune.defs.ZoneNames;
import autosimmune.env.Environment;
import autosimmune.env.Global;
import autosimmune.env.Tissue;
import autosimmune.utils.Affinity;
import autosimmune.utils.Pattern;

/**
 * Classe representando a Célula Macrófago
 * @author maverick
 *
 */
@AgentAnnot(displayName = "Macrophage Cell")
public class Macrophage extends APC {
	/** Número de Macrophage */
	protected static Integer numMacrophages = 0;

	/** Representação dos estados internos da célula */
	private MacrophageStates state;
	
	/** Estado anterior da celula */
	private MacrophageStates previous_state;
	
	private int lifetime;

	private int mk1duration;
	
	private int numReleasesCytokineNecrotic;
	
	/**
	 * Construtor da Classe
	 * @param z Zona a qual o macrófago pertence inicialmente
	 * @param self Peptídeo self que identifica o macrófago
	 * @param x Coordenada X inicial
	 * @param y Coordenada Y inicial
	 */
	public Macrophage(Environment z, int x, int y) {
		super(z, x, y, new Pattern(Global.getInstance().getStringParameter(EnvParameters.MACROPHAGE_SELF_PATTERN)));
		numMacrophages++;
		initiateParameters();
	}
	
	private void initiateParameters(){
		this.state = MacrophageStates.NORMAL;
		this.previous_state = MacrophageStates.NORMAL;
		this.lifetime = Global.getInstance().getIntegerParameter(EnvParameters.MACROPHAGE_LIFETIME);
		this.mk1duration = Global.getInstance().getIntegerParameter(EnvParameters.MACROPHAGE_MK1_DURATION);
		this.tcruzis = new ArrayList<TCruzi>();
		this.numReleasesCytokineNecrotic = Global.getInstance().getIntegerParameter(EnvParameters.CYTOKINE_NECROTIC_NUM_RELEASES);
	}
	
	/**
	 * Retorna o numero de instancias
	 */
	public static int getNumMacrophage(){
		return numMacrophages;
	}
	
	/**
	 * Método que executa o passo do macrofago a cada tick
	 */
	@ScheduledMethod(start = 0, interval = 1)
	public void step(){
		
		if (getTicks() > lifetime){
			changeState(MacrophageStates.APOPTOSIS);
		}
		
		/*reproducao dos parasitas*/
		reproducingParasites();
		
		switch(state){

			case NORMAL: {
				followCitokineByGradient(true, CitokineNames.PK1, CitokineNames.APOPTOSIS, CitokineNames.NECROTIC);
				
				double necrotic = getCitokineValue(CitokineNames.NECROTIC);
				double pk = getCitokineValue(CitokineNames.PK1);
				double apop = getCitokineValue(CitokineNames.APOPTOSIS);

				if (necrotic > 0 || pk > apop ){
					changeState(MacrophageStates.INFLAMATORY);
				}
				
			} break;
			
			case INFLAMATORY: {
				
				if(getCitokineValue(CitokineNames.NECROTIC) > 0){
					followCitokineByGradient(true, CitokineNames.NECROTIC);
				} else if (getCitokineValue(CitokineNames.PK1) > 10){
					followCitokineByGradient(true, CitokineNames.PK1);
				} else {
					randomWalk();
				}
				
				for(PC pc: getEspecificNeighbors(PC.class)){
					if (pc.isDead()){
						//os antigenos capturados na fagocitose de uma celula infectada
						//sao semelhantes aos apresentados pelo MHCI
						this.antigen = pc.MHCI();
						pc.clean();
						changeState(MacrophageStates.ACTIVE);
					}
				}
				
				for(Antibody ab: getEspecificNeighbors(Antibody.class)){
					ab.clean();
				}
				
				PAMPS pamps = PAMPS.getInstance();
				for(Virus v: getEspecificNeighbors(Virus.class)){
					for(Pattern p: pamps.getPamps()){
						if (Affinity.match(p, v.getSelf())){
							if (v != null) {
								if(v.neutralize()){
									changeState(MacrophageStates.ACTIVE);
								}
							}
						}
					}
				}
				
				for(TCruzi tc: getEspecificNeighbors(TCruzi.class)){
					if(hasSpace())
						endocitarBy(tc);
				}
				
			} break;
			
			case ACTIVE: {
				
				if(getCitokineValue(CitokineNames.NECROTIC) > 0){
					followCitokineByGradient(true, CitokineNames.NECROTIC);
				} else if (getCitokineValue(CitokineNames.PK1) > 10){
					followCitokineByGradient(true, CitokineNames.PK1);
				} else {
					randomWalk();
				}
				
				if(mk1duration > 0){
					releaseCitokine(CitokineNames.MK1);
					mk1duration--;
				}
				/*remove Células PC mortas*/
				for(PC pc: getEspecificNeighbors(PC.class)){
					if (pc.isDead()){
						pc.clean();
					}
				}
				/*remove Células Macrophage mortas*/
				for(Macrophage ma: getEspecificNeighbors(Macrophage.class)){
					if (ma.isDead()){
						ma.clean();
					}
				}
				
				for(Antibody ab: getEspecificNeighbors(Antibody.class)){
					ab.clean();
				}
				
				PAMPS pamps = PAMPS.getInstance();
				for(Virus v: getEspecificNeighbors(Virus.class)){
					for(Pattern p: pamps.getPamps()){
						if (Affinity.match(p, v.getSelf())){
							if (v != null) {
								v.neutralize();
							}
						}
					}
				}
				
				for(TCruzi tc: getEspecificNeighbors(TCruzi.class)){
					if(hasSpace())
						endocitarBy(tc);
				}
				
			} break;

			case APOPTOSIS: {
				apoptosis();
			}break;
		
			case NECROSIS: {
				if(this.previous_state != MacrophageStates.NECROSIS){
					removeParasites();
					releaseCitokine(CitokineNames.NECROTIC);
					changeState(MacrophageStates.NECROSIS);
				}else{
					if(getTicks() % Global.getInstance().getIntegerParameter(EnvParameters.CYTOKINE_NECROTIC_INTERVAL_RELEASES) == 0 && numReleasesCytokineNecrotic > 0){
						releaseCitokine(CitokineNames.NECROTIC);
						numReleasesCytokineNecrotic--;
					}
				}
			}break;
		}
	}

	@Override
	public boolean infectedBy(Virus virus) {
		return false;
	}

	@Override
	public void contact(boolean b) {
		//TODO parametrizar fatores de prolongamento de vida
		mk1duration+=10;
		lifetime+=10;
	}
	
	/**
	 * Informa se a célula está morta
	 * @return
	 */
	public boolean isDead() {
		return (this.state == MacrophageStates.APOPTOSIS || this.state == MacrophageStates.NECROSIS);
	}

	/**
	 * Remove uma celula morta do tecido.
	 * Funcao chamada quando um macrofago remove a celula morta
	 */
	@Override
	public void clean() {
		removeParasites();
		zone.removeAgent(this);
		numMacrophages--;
	}
	
	/**
	 * Mata a celula agressivamente
	 */
	@Override
	public void necrosis() {
		changeState(MacrophageStates.NECROSIS);
	}
	
	/**
	 * Chama a funcao de reproducao dos parasitas
	 */
	public void reproducingParasites(){
		/*tcruzi*/
		if(this.tcruzis != null){
			int numTcruzi = this.getTCruzis().size();
			int numTCruziBreach = Global.getInstance().getIntegerParameter(EnvParameters.TCRUZI_NUM_BREACH);
			for(int i = 0; i<numTcruzi;i++){
				if(this.getTCruzis().size() < numTCruziBreach){
					this.getTCruzis().get(i).multiplica(this);
				}else{
					necrosis();
					break;
				}
			}
		}
	}
	
	/**
	 * Altera o estado atual da celula
	 * @param newstate
	 */
	private void changeState(MacrophageStates newstate){
		previous_state = state;
		state = newstate;
	}
}