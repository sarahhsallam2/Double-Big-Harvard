package projca.src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
//TODO we didnt use the 16 bits for instruction memory
//TODO we didnt use the 8 bits of data memory 
public class readfile {
private static int[] dataMemory= new int [2048];
private static Object[] instructionMemory =new Object[1024] ;
private static int[] registerFile  =new int[64] ;
private static Object[] pipeFetchDecode = new Object [4];
private static Object[] pipeDecodeExecute = new Object [9];
private static Object[] instructionInAssembly  =new Object[1024] ;

private static ArrayList<String> instructions=new ArrayList<>();
private static int noOfInstructions=0;
private static int carryBit = 0;
private static int signBit =0;
private static int overflowBit =0;
private static int zeroBit=0;
private static int negativeBit=0;
private static int pc = 0;
private static int clockcycles=1;
private static String statusRegister="";
private static boolean branchFlag= false ;
private static int branchCounter=0;
private static int stillExecuting= -1 ;
private static boolean lastExuecution= false;

private static int Opcode =0; // bits: 15-->12 
private static int R1= 0 ; // bits: 11-->6 
private static int R2= 0 ; // bits: 5-->0 
private static int Imm =0 ;  // bits: 5-->0 
private static int valueR1 =0;
private static int valueR2 =0 ;

public void readfile() {
	String filePath= "assembly.txt";
    String line;
		try {
			
          BufferedReader br = new BufferedReader(new FileReader(filePath));
          while ((line = br.readLine()) != null) {
        	  instructions.add(line);
        	  noOfInstructions++;
        	}
          
        	} 
		catch (Exception e) {
        	e.printStackTrace();
        	}
	}

public static void runclkcyles(int instruction,int Opcode,int R1,int R2,int Imm, int valueR1,int valueR2) {
	int maxCylces= 3+ (noOfInstructions-1)*1;

	while (clockcycles <= maxCylces) {
		if (clockcycles == 1) {
			fetch();
		} else if (clockcycles == 2) {
		    execute(Opcode,R1,R2,Imm,valueR1,valueR2);
			fetch();
		} else if (clockcycles == maxCylces - 1) {
		    execute(Opcode,R1,R2,Imm,valueR1,valueR2);
			decode(instruction);
		} else if (clockcycles == maxCylces) {
		    execute(Opcode,R1,R2,Imm,valueR1,valueR2);
		} else {
		    execute(Opcode,R1,R2,Imm,valueR1,valueR2);
			decode(instruction);
			fetch();
		}
		clockcycles++;

}
}

public static void LoadInstMemory() {
		int j=0;
		for (String str : instructions) {
			instructionInAssembly[j]= str;
			//System.out.println(instructionInAssembly[j]);
			String[] parts = str.split(" "); // split the string by space
			String opCode = "";
			String  rs =""  ;
			String  rt = "";
		
				
		    opCode=SwitchOpcode(parts[0].toUpperCase());
			rs=SwitchReg(parts[1].toUpperCase());
				
				
               if(isRtype(opCode)) {
            		  rt=SwitchReg(parts[2].toUpperCase());
               //System.out.println("rt bgd  "+ rt);
               }
               
               else {
            		 if(parts[2].charAt(0)=='-') {
            			 
            			 rt = toBinary(getNegative(Integer.parseInt(removeFirstChar(parts[2])) ,6 ),6);
            			// System.out.println(temp);
            		 }
            		 else {
                     String binaryValue =  Integer.toBinaryString(Integer.parseInt(parts[2]));
                     String zeroes = "";
                     
            		   for  (int i =0 ; i<6-binaryValue.length(); i++) {
            			   zeroes+=0;
            		   }
            		   rt=zeroes+binaryValue;
            		   //System.out.println("rt imm  "+ rt);
            	       //System.out.println(rt);	
            		 }
            	  }	
               
            // System.out.println("rt  "+ rt);
            String FullInst = opCode + rs + rt ;
           // System.out.println(FullInst);
            int IntegerInstruction = Integer.parseInt(FullInst,2);
           // System.out.println(IntegerInstruction);
            instructionMemory[j]=IntegerInstruction;
            j++;
            
            
            //System.out.println(FullInst);
		}
	}
//public static void fetch() {
//	pc = registerFile[65];
//    Object instruction = null;
//    
//   // for(int i = 0; i < instructionMemory.length && instructionMemory[pc]!=null; i++) {
//    	
//        instruction = instructionMemory[pc];
//        //clockcycles++;
//        //decode(instruction);
//        pc++;
//        registerFile[65]=pc;
////        if (clockcycles % 3 == 1) {
////            int instructionIndex = (clockcycles - 1) / 3;
////            System.out.println("IF Stage: Instruction " + (instructionIndex + 1));
////        }
//        if (instructionMemory[pc]==null) {
//        	  clockcycles+=2;
//        }
//   // }
//}

public static Object fetch() {
	
    //Object instruction = null;
    Object instruction = instructionMemory[pc];
   
    //if (instructionMemory[pc])
    pipeFetchDecode[1]=(int) instruction;
    pipeFetchDecode[2]=pc;
    pipeFetchDecode[3]= instructionInAssembly[pc];
    pc++;
    return instruction;
   
 }

public static void decode(int instruction) {
	 Opcode =0; // bits: 15-->12 
	 R1= 0 ; // bits: 11-->6 
	 R2= 0 ; // bits: 5-->0 
	 Imm =0 ;  // bits: 5-->0 
	 valueR1 =0;
	 valueR2 =0 ;
	
	
	int temp = 0;
	
	Opcode = instruction >>> 12 ;
    
    temp = instruction & 0b0000111111000000;
    R1 = temp >>6 ;
    R2 = instruction & 0b0000000000111111;
    Imm =instruction & 0b0000000000111111;
    valueR1= (int) registerFile[R1];
    valueR2= (int) registerFile[R2];
    pipeDecodeExecute[1]= Opcode ;
    pipeDecodeExecute[2]= R1 ;
    pipeDecodeExecute[3]= R2;
    pipeDecodeExecute[4]= twoComplementToInt(toBinary(Imm, 6));
    pipeDecodeExecute[5]= valueR1 ;
    pipeDecodeExecute[6]= valueR2;
    pipeDecodeExecute[7]= pipeFetchDecode[2];
    pipeDecodeExecute[8]= pipeFetchDecode[3];
    if(Opcode==7) {
    	pipeFetchDecode[1]=null;
        pipeFetchDecode[2]=null;
    	pipeFetchDecode[3]=null;
        
    }
    
}

public static void execute(int Opcode,int R1,int R2,int Imm, int valueR1,int valueR2 ) {
	
	switch(Opcode ) {
	 case 0:
        ADD(R1,valueR1,valueR2);
        break;
    case 1:
        SUB(R1,valueR1,valueR2);
        break;
    case 2:
        MUL(R1,valueR1,valueR2);
        break;
    case 3:
        MOVI(R1	,Imm);
        break;
    case 4:
        BEQZ(R1,Imm);
        break;
    case 5:
        ANDI(R1,valueR1,Imm);
        break;
    case 6:
        EOR(R1,valueR1,valueR2);
        break;
    case 7:
        BR(R1,R2);
        break;
    case 8:
        SAL(R1,valueR1,Imm);
        break;
    case 9:
        SAR(R1,valueR1,Imm);
        break;
    case 10:
        LDR(R1,R2);
        break;
    case 11:
        STR(R1,R2);
        break;
}
	statusRegister = "000" + carryBit + overflowBit + negativeBit + signBit + zeroBit;
	carryBit=0;
	overflowBit=0;
	negativeBit=0;
	signBit=0;
	zeroBit=0;
	
	

	}

public static String PrintDataMemory() {
	String s="";
	for (int i=0;i<dataMemory.length;i++) {
		s=s+"Data" +" "+ i + " " +dataMemory[i]+" ,  ";
	}
	return s;
}

public static String PrintRegisterFile() {
	String s="";
	for (int i=0;i<registerFile.length ;i++) {
		s=s+"Register value" +" "+i + " " +registerFile[i]+"   ,  ";
	}
	return s;
}

//public void PipelineExecuteCycle() {
//	if (pc==0) {
//		fetch();
//	}
//	if (pc==1) {
//		Object instruction = pipeFetchDecode[1];
//		decode((int)instruction);
//		fetch();
//	}
//	if (instructionMemory[pc]==null) {
//		execute((int)pipeDecodeExecute[1], (int)pipeDecodeExecute[2],(int)pipeDecodeExecute[3], (int)pipeDecodeExecute[4], (int)pipeDecodeExecute[5], (int) pipeDecodeExecute[6]);
//		decode((int)pipeFetchDecode[1]);
//		execute((int)pipeDecodeExecute[1], (int)pipeDecodeExecute[2],(int)pipeDecodeExecute[3], (int)pipeDecodeExecute[4], (int)pipeDecodeExecute[5], (int) pipeDecodeExecute[6]);
//		
//	}
//	else {
//		execute((int)pipeDecodeExecute[1], (int)pipeDecodeExecute[2],(int)pipeDecodeExecute[3], (int)pipeDecodeExecute[4], (int)pipeDecodeExecute[5], (int) pipeDecodeExecute[6]);
//		decode((int)pipeFetchDecode[1]);
//		fetch();
//	}
//	
//	clockcycles++;
//		
//	
//}


public void PipelineExecuteCycle() {
System.out.println("Cycle" + " "+clockcycles);
if (pc==0) {
	System.out.println("Fetching instruction " + pc + "     "+instructionInAssembly[pc]);
	System.out.println("Input : pc value = " + pc );
	System.out.println("" );
	fetch();
}
else {
	if (pipeDecodeExecute[1]!=null && pipeDecodeExecute[2]!=null && pipeDecodeExecute[3]!=null && pipeDecodeExecute[4]!=null && pipeDecodeExecute[5]!=null && pipeDecodeExecute[6]!=null ) {
		if( stillExecuting==1 ) {
			lastExuecution= true;
			}
		System.out.println("Executing instruction " + pipeDecodeExecute[7] + "      "+pipeDecodeExecute[8]);
		if((int)pipeDecodeExecute[1]==10){
			System.out.println("Inputs : Opcode = " + (int)pipeDecodeExecute[1] + " ,  1stReg = " + (int)pipeDecodeExecute[2]+   " , 2ndReg = " + (int)pipeDecodeExecute[3]+  " ,Value Of R"+ (int)pipeDecodeExecute[2]+ " = " + (int)pipeDecodeExecute[5]+  " , Memory Address "  + (int)pipeDecodeExecute[3]+ " = " + dataMemory[(int)pipeDecodeExecute[3]] );

		 }
		else if((int)pipeDecodeExecute[1]==11) {
			System.out.println("Inputs : Opcode = " + (int)pipeDecodeExecute[1] + " ,  1stReg = " + (int)pipeDecodeExecute[2]+ " ,  Memory Address = " + (int)pipeDecodeExecute[4]+  " , Value Of R"+ (int)pipeDecodeExecute[2]+ " = " + (int)pipeDecodeExecute[5]);
    
      }
		else if(isRtypeint((int)pipeDecodeExecute[1])) {
			System.out.println("Inputs : Opcode = " + (int)pipeDecodeExecute[1] + " ,  1stReg = " + (int)pipeDecodeExecute[2]+   " , 2ndReg = " + (int)pipeDecodeExecute[3]+  " ,Value Of R"+ (int)pipeDecodeExecute[2]+ " = " + (int)pipeDecodeExecute[5]+  " , Value Of R"  + (int)pipeDecodeExecute[3]+ " = " + (int)pipeDecodeExecute[6] );
    
      }
	
		 else {

				System.out.println("Inputs : Opcode = " + (int)pipeDecodeExecute[1] + " ,  1stReg = " + (int)pipeDecodeExecute[2]+ " ,  Immediate = " + (int)pipeDecodeExecute[4]+  " , Value Of R"+ (int)pipeDecodeExecute[2]+ " = " + (int)pipeDecodeExecute[5]);

			 
		 }
		//System.out.println("Inputs : Opcode = " + (int)pipeDecodeExecute[1] + " ,  1stReg = " + (int)pipeDecodeExecute[2]+   " , 2ndReg = " + (int)pipeDecodeExecute[3]+  " ,  Immediate = " + (int)pipeDecodeExecute[4]+  " , Value Of R"+ (int)pipeDecodeExecute[2]+ " = " + (int)pipeDecodeExecute[5]+  " , Value Of R"  + (int)pipeDecodeExecute[3]+ " = " + (int)pipeDecodeExecute[6] );
		System.out.println("" );
	   
		
		//System.out.println("Before execution Register File:  \n " + PrintRegisterFile());
		//System.out.println("Before execution Data Memory:  \n " + PrintDataMemory());
		System.out.println("" );
		//System.out.println("" );
		execute((int)pipeDecodeExecute[1], (int)pipeDecodeExecute[2],(int)pipeDecodeExecute[3], (int)pipeDecodeExecute[4], (int)pipeDecodeExecute[5], (int) pipeDecodeExecute[6]);
		
		if(pipeDecodeExecute[1]!= null) {
			if((int)pipeDecodeExecute[1]!= 3 && (int)pipeDecodeExecute[1]!= 4 && (int)pipeDecodeExecute[1]!= 7 && (int)pipeDecodeExecute[1]!= 10 && (int)pipeDecodeExecute[1]!= 11 ) {
		
		
		    	System.out.println("Status Register: "+ statusRegister );
		    }
		}
		
		if(pipeDecodeExecute[2]!=null && isOutputR1((int)pipeDecodeExecute[1])) {
		System.out.println("Outputs : Value Of R"+(int)pipeDecodeExecute[2]+ "  = " + registerFile[(int)pipeDecodeExecute[2]]);
		}
		else if (pipeDecodeExecute[1]!=null && (int)pipeDecodeExecute[1]==11) {
			System.out.println("Outputs : Value Of Data Memory "+(int)pipeDecodeExecute[4]+ "  = " + (int)pipeDecodeExecute[5]);	
		}
		else {
			System.out.println("Outputs : New PC to be fetched = "+ pc);
	
		}

		//System.out.println("After execution Register File:  \n " + PrintRegisterFile());
		//System.out.println("After execution Data Memory:  \n " + PrintDataMemory());
		System.out.println("" );
		System.out.println("        ********         " );
		
	}
	
	 if(pipeFetchDecode[1]!=null  )  {
		 System.out.println("Decoding instruction " + pipeFetchDecode[2] + "      "+pipeFetchDecode[3]);
		 System.out.println("Inputs : instruction = " + (int)pipeFetchDecode[1]);
		 System.out.println("" );
		 System.out.println("        ********         " );
         System.out.println("" );
		 //decode(int instruction)
		decode((int)pipeFetchDecode[1]);
	 }
	 
     if(instructionMemory[pc]!=null & !branchFlag) {
    System.out.println("Fetching instruction " + pc + "     "+instructionInAssembly[pc]);
    System.out.println("Input : pc value = " + pc );
	System.out.println("" );
	 fetch();
	}
     else if (branchFlag && branchCounter>0) {
    	 pipeFetchDecode[1]=null;
    	 pipeFetchDecode[2]=null;
    	 pipeFetchDecode[3]=null;
    	 branchCounter--;
    	 if(branchCounter==0) {
    		 branchFlag=false;
    	 }
     }
     else {
    	 pipeFetchDecode[1]=null;
    	 pipeFetchDecode[2]=null;
    	 pipeFetchDecode[3]=null;
    	 stillExecuting= 1; 
     }
}

clockcycles++;
	
}

public static void ADD(int R1,int valueR1,int valueR2) {
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	 int resultval = valueR1 + valueR2;
//	 registerFile[R1]= resultval;
	 IntegerToTwosComplement=  intToTwoComplement(resultval,9);
	 //System.out.println("NNNNNNNNN" +IntegerToTwosComplement);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	 //System.out.print("NNNNNNNNN" +resultbit1to9);
	 result = twoComplementToInt(resultbit1to9);
   //System.out.println("     NNNNNNNNN" +result);
	 registerFile[R1]= result;
	carryBit = getCarry(valueR1,valueR2);
	overflowBit = Overflowadd(result,valueR1,valueR2);
	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	signBit = setSign(result ,valueR1,valueR2);
	
}	

public static String removeFirstChar(String input) {
    if (input == null || input.isEmpty()) {
        return input;     }
    return input.substring(1); 
}

public static int getNegative(int number, int numBits) {
    int mask = (1 << numBits) - 1;
    int negNumber = (~number + 1) & mask;
    return negNumber;
}

public static int twoComplementToInt(String binaryString) {
    if (binaryString.charAt(0) == '1') {
        String flipped = flipBits(binaryString);
        int complement = Integer.parseInt(flipped, 2);
        return -(complement + 1);
    } else {
        return Integer.parseInt(binaryString, 2);
    }
}

public static String flipBits(String binaryString) {
    StringBuilder flipped = new StringBuilder();
    for (char bit : binaryString.toCharArray()) {
        flipped.append(bit == '0' ? '1' : '0');
    }
    return flipped.toString();
}

public static String intToTwoComplement(int number, int numberOfBits) {
    int[] binary = new int[numberOfBits];
    String resultString=" ";
        for (int i = numberOfBits - 1; i >= 0; i--) {
        int bit = (number >> i) & 1;
        int nbits= numberOfBits - 1 - i;
        binary[nbits] = bit;
    }

        StringBuilder binarystring = new StringBuilder();
    for (int bit : binary) {
    	binarystring.append(bit);
    }
    resultString= binarystring.toString();
    return resultString;
}

public static void SUB(int R1,int valueR1,int valueR2) {
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	result= valueR1 - valueR2;
	registerFile[R1]=result;
	IntegerToTwosComplement=  intToTwoComplement(result,9);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	result = twoComplementToInt(resultbit1to9);
	overflowBit = Overflowsub(result,valueR1,valueR2);
	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	signBit = setSign(result ,valueR1,valueR2);
	
}

public static void MUL(int R1,int valueR1,int valueR2) {
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	result= valueR1 * valueR2;
	registerFile[R1]= result;
	IntegerToTwosComplement=  intToTwoComplement(result,9);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	result =  twoComplementToInt(resultbit1to9);
	   //System.out.println("     NNNNNNNNNmul" +result);

	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	

}

public static void MOVI(int R1,int immediate) {
	registerFile[R1] = immediate;
}

public static void BEQZ(int R1,int immediate) {
	if(registerFile[R1]==0) {
		pc = (int)pipeDecodeExecute[7] +1 + immediate;
		branchFlag= true ;
		branchCounter=1;
		pipeFetchDecode[1]=null;
	    pipeFetchDecode[2]=null;
		pipeFetchDecode[3]=null;
	    pipeDecodeExecute[1]=null;
		pipeDecodeExecute[2]=null;
		pipeDecodeExecute[3]=null;
		pipeDecodeExecute[4]=null;
		pipeDecodeExecute[5]=null;
		pipeDecodeExecute[6]=null;
	}
}

public static void ANDI(int R1,int valueR1,int immediate) {
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	result=  valueR1 & immediate;
	IntegerToTwosComplement=  intToTwoComplement(result,9);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	 result = twoComplementToInt(resultbit1to9);
	registerFile[R1]= result;
	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	

}

public static void EOR(int R1,int valueR1,int valueR2) { 
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	result=  valueR1 ^ valueR2;
	registerFile[R1]= result;
	IntegerToTwosComplement=  intToTwoComplement(result,9);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	 result = twoComplementToInt(resultbit1to9);
	//registerFile[R1]= result;
	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	

}

public static void BR(int R1,int R2) {
	String r1binary= toBinary((int) registerFile[R1],8);
	String r2binary= toBinary((int) registerFile[R2],8);
	String concatenatedString = r1binary + r2binary;
	//System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA   "+registerFile[R2]);
	int concatenatedInt = Integer.parseInt(concatenatedString,2);
	pc = concatenatedInt; 
	branchFlag= true ;
	branchCounter=1;
	pipeFetchDecode[1]=null;
    pipeFetchDecode[2]=null;
	pipeFetchDecode[3]=null;
    pipeDecodeExecute[1]=null;
	pipeDecodeExecute[2]=null;
	pipeDecodeExecute[3]=null;
	pipeDecodeExecute[4]=null;
	pipeDecodeExecute[5]=null;
	pipeDecodeExecute[6]=null;
	
}

public static void SAL(int R1,int valueR1,int immediate) {
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	result=   valueR1 << immediate;
	registerFile[R1]= result;
	IntegerToTwosComplement=  intToTwoComplement(result,9);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	result = twoComplementToInt(resultbit1to9);
	
	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	

}

public static void SAR(int R1,int valueR1,int immediate) {
	
	int result=0;
	String resultbit1to9= " ";
	String IntegerToTwosComplement=" ";
	result=  valueR1 >> immediate;
	registerFile[R1]= result;
	IntegerToTwosComplement=  intToTwoComplement(result,9);
	resultbit1to9= getSubstring(IntegerToTwosComplement,0,9);
	 result = twoComplementToInt(resultbit1to9);
	
	zeroBit = zeroFlag(result);
	negativeBit= negative(result);
	
	

}

public static void LDR(int R1,int R2) {
	registerFile[R1]= dataMemory[R2];
}

public static void STR(int R1,int R2) {
	dataMemory[R2]= (int) registerFile[R1];

}

public static String toBinary(int number, int length) {
	    String binaryString = Integer.toBinaryString(number);
	    int paddingLength = Math.max(0, length - binaryString.length());
	    String padding = "0".repeat(paddingLength);
	    return padding + binaryString;
	}

public static String getSubstring(String input, int startIndex, int endIndex) {
        startIndex = Math.max(startIndex, 0);
    endIndex = Math.min(endIndex, input.length());

    return input.substring(startIndex, endIndex);
}

public static int xorinteger(int x,int y) {
	if(x==0&& y==0) {
		return 0;
	}
	else if(x==0&& y==1) {
		return 1;
	}
	else if(x==1&& y==0) {
		return 1;
	}
	else if(x==1&& y==1) {
		return 1;
	}
	return 0;
}

public static int xorString(char x,char y) {
	if(x=='0' && y=='0') {
		return 0;
	}
	else if(x=='0' && y=='1') {

		return 1;
	}
	else if(x=='1' && y=='0') {

		return 1;
	}
	else if(x=='1' && y=='1') {

		return 0;
	}
	return 0;
}

public static int getCarry(int valueR1 , int valueR2) {
   // System.out.print(valueR1);
	int carry = 0;
	int count1s=0;
    String binaryR1= toBinary(valueR1,8);
    String binaryR2= toBinary(valueR2,8);
    char R1bit7 = binaryR1.charAt(0) ;
	char R2bit7= binaryR2.charAt(0);
	if(R1bit7 =='1' && R2bit7=='1') {
		carry = 1;
		count1s++;
	}
	if(R1bit7 =='1') {
		count1s++;
	}
	if(R2bit7 =='1') {
		count1s++;
	}
	if(count1s>=2 ) {
		return 1;
	}
	return 0;
}

//public static int Overflow(int result,int r1,int r2) {
//	String resultwithCarry= intToTwoComplement(result,9);
//	String R1= intToTwoComplement(r1,8);
//	String R2= intToTwoComplement(r2,8);
//	char resultbit9= resultwithCarry.charAt(0);
//	char resultbit8 = resultwithCarry.charAt(1);
//	char r1bit8 = R1.charAt(0);
//	char r2bit8 = R2.charAt(0);
//	String tobeXored = resultbit9 + resultbit8 + r1bit8 + r2bit8 +"" ;
//	int m = xorString(resultbit9,resultbit8);
//	int n = xorString(r1bit8,r2bit8);
//	int res = xorinteger(m,n);
//	return xorOperation(tobeXored);
//}
public static int Overflowadd(int result,int r1,int r2) {
	String resultwithCarry= intToTwoComplement(result,9);
	String R1= intToTwoComplement(r1,9);
	String R2= intToTwoComplement(r2,9);
	boolean firstp = false ;
	boolean firstn = false ;
	boolean secondp = false ;
	boolean secondn = false ;
	char resultbit9= resultwithCarry.charAt(0);
	char r1bit8 = R1.charAt(0);
	char r2bit8 = R2.charAt(0);
	if(r1bit8=='1' ) {
		firstn= true ;
		
	}
	if(r2bit8=='1') {
		secondn=true;
		
	}
	if(r1bit8=='0' ) {
		firstp= true ;
		
	}
	if(r2bit8=='0') {
		secondp=true;
		
	}
	if(firstn && secondn && resultbit9=='0') {
		return 1;
	}
	else if(firstp && secondp && resultbit9=='1') {
		return 1;
	}
	else {
		return 0;
	}

}
public static int Overflowsub(int result,int r1,int r2) {
	String resultwithCarry= intToTwoComplement(result,9);
	String R1= intToTwoComplement(r1,9);
	String R2= intToTwoComplement(r2,9);
	boolean firstp = false ;
	boolean firstn = false ;
	boolean secondp = false ;
	boolean secondn = false ;
	char resultbit9= resultwithCarry.charAt(0);
	char r1bit8 = R1.charAt(0);
	char r2bit8 = R2.charAt(0);
	if(r1bit8=='1' ) {
		firstn= true ;
		
	}
	if(r2bit8=='1') {
		secondn=true;
		
	}
	if(r1bit8=='0' ) {
		firstp= true ;
		
	}
	if(r2bit8=='0') {
		secondp=true;
		
	}
	if(firstn && secondp && resultbit9=='0') {
		return 1;
	}
	else if(firstp && secondn && resultbit9=='1') {
		return 1;
	}
	else {
		return 0;
	}

}


public static int xorOperation(String binaryString) {
    int result = 0;
    for (int i = 0; i < binaryString.length(); i++) {
        char c = binaryString.charAt(i);
        if (c == '1') {
            result ^= 1;
        }
    }
    return result;
}

public static int zeroFlag(int result) {
	if(result==0)
		return 1;
	return 0;
	
}

public static int setSign(int result , int valueR1 , int valueR2) {
	int carryBit = getCarry(valueR1,valueR2);
	int negativeBit= negative(result);
	int resultinteger = xorinteger(carryBit,negativeBit);
	return resultinteger;
}

public static int negative(int result) {
	String bit8= intToTwoComplement(result,9);
	if(bit8.charAt(0)=='1') {
		return 1;
	}
	return 0;
}

public static String intToBinary(int num) {
    return Integer.toBinaryString(num);
}

public static boolean isRtype(String opCode) {
		
		if (opCode.equals("0000") ||
			opCode.equals("0001") ||
			opCode.equals("0010") || 
			opCode.equals("0110") || 
			opCode.equals("0111"))
			return true;
		return false;
		
	}

public static boolean isRtypeint(int opCode) {
	
	if (opCode==0 ||
		opCode==1||
		opCode==2 || 
		opCode==6 || 
		opCode==7)
		return true;
	return false;
	
}

public static boolean isOutputR1(int opCode) {
	
	if (opCode==0 ||
		opCode==1||
		opCode==2 || 
		opCode==3 || 
		opCode==5 ||
		opCode==6 ||
		opCode==8 ||
		opCode==9 ||
		opCode==10 )
		return true;
	return false;
	
}
	
public static String SwitchReg(String Register) {
		String rs = " ";
		switch (Register) {
		case "R0": rs="000000";	break; 
		case "R1": rs="000001";	break;
		case "R2": rs="000010";	break;
		case "R3": rs="000011";	break;
		case "R4": rs="000100";	break;
		case "R5": rs="000101";	break;
		case "R6": rs="000110";	break;
		case "R7": rs="000111";	break;
		case "R8": rs="001000";	break;
		case "R9":  rs="001001";	break;
		case "R10": rs="001010";	break;
		case "R11": rs="001011";	break;
		case "R12": rs="001100";	break;
		case "R13": rs="001101";	break;
		case "R14": rs="001110";	break;
		case "R15": rs="001111";	break;
		case "R16": rs="010000";	break;
		case "R17": rs="010001";	break;
		case "R18": rs="010010";	break;
		case "R19": rs="010011";	break;
		case "R20": rs="010100";	break;
		case "R21": rs="010101";	break;
		case "R22": rs="010110";	break;
		case "R23": rs="010111";	break;
		case "R24": rs="011000";	break;
		case "R25": rs="011001";	break;
		case "R26": rs="011010";	break;
		case "R27": rs="011011";	break;
		case "R28": rs="011100";	break;
		case "R29": rs="011101";	break;
		case "R30": rs="011110";	break;
		case "R31": rs="011111";	break;
		case "R32": rs="100000";	break;
		case "R33": rs="100000";	break;
		case "R34": rs="100010";	break;
		case "R35": rs="100011";	break;
		case "R36": rs="100100";	break;
		case "R37": rs="100101";	break;
		case "R38": rs="100110";	break;
		case "R39": rs="100111";	break;
		case "R40": rs="101000";	break;
		case "R41": rs="101001";	break;
		case "R42": rs="101010";	break;
		case "R43": rs="101011";	break;
		case "R44": rs="101100";	break;
		case "R45": rs="101101";	break;
		case "R46": rs="101110";	break;
		case "R47": rs="101111";	break;
		case "R48": rs="110000";	break;
		case "R49": rs="110001";	break;
		case "R50": rs="110010";	break;
		case "R51": rs="110011";	break;
		case "R52": rs="110100";	break;
		case "R53": rs="110101";	break;
		case "R54": rs="110110";	break;
		case "R55": rs="110111";	break;
		case "R56": rs="111000";	break;
		case "R57": rs="111001";	break;
		case "R58": rs="111010";	break;
		case "R59": rs="111011";	break;
		case "R60": rs="111100";	break;
		case "R61": rs="111101";	break;
		case "R62": rs="111110";	break;
		case "R63": rs="111111";	break;
		
		default:break;

		}
		return rs;
		
	}
	
public static String SwitchOpcode(String Opcode ) {
		String OpcodeBin="";
		switch (Opcode) {
		
		
		case "ADD":  OpcodeBin="0000";	break;
		case "SUB":  OpcodeBin="0001";	break;
		case "MUL":  OpcodeBin="0010";	break;
		case "MOVI": OpcodeBin="0011";	break;
		case "BEQZ": OpcodeBin="0100";	break;
		case "ANDI": OpcodeBin="0101";	break;
		case "EOR":  OpcodeBin="0110";	break;
		case "BR":   OpcodeBin="0111";	break;
		case "SAL":  OpcodeBin="1000";	break;
		case "SAR":  OpcodeBin="1001";	break; 
		case "LDR":  OpcodeBin="1010";	break;
		case "STR":  OpcodeBin="1011";	break;
		default:break;

		}
		return OpcodeBin;
	}


public static void mainForGui() {
	readfile f= new readfile();
	f.readfile();
	LoadInstMemory();
	//registerFile[65]=pc;
	int c=0;
	for (int i=0 ; i<f.instructionMemory.length;i++) {
		if (instructionMemory[i]==null)
			break;
		c++;
	}
	
	
//	for (int i=0;i<c+2;i++) {
//	f.PipelineExecuteCycle();
//	System.out.println("--------------------------------------------------------------------------------");
//	}
	
	while( !lastExuecution ) {
		f.PipelineExecuteCycle();
	    System.out.println("--------------------------------------------------------------------------------");
    
	}
	
	//ystem.out.println(PrintDataMemory());
//	System.out.println("After execution Register File:  \n " + PrintRegisterFile());
	System.out.println("");
	System.out.println("");
	System.out.println("After execution Register File:  \n " + PrintRegisterFile());
	System.out.println("");
	System.out.println("");
	System.out.println("After execution Data Memory:  \n " + PrintDataMemory());

}
public static void main(String[] args) {
		readfile f= new readfile();
		f.readfile();
		LoadInstMemory();
		//registerFile[65]=pc;
		int c=0;
		for (int i=0 ; i<f.instructionMemory.length;i++) {
			if (instructionMemory[i]==null)
				break;
			c++;
		}
		
		
//		for (int i=0;i<c+2;i++) {
//		f.PipelineExecuteCycle();
//		System.out.println("--------------------------------------------------------------------------------");
//		}
		
		while( !lastExuecution ) {
			f.PipelineExecuteCycle();
		    System.out.println("--------------------------------------------------------------------------------");
	    
		}
		
		//ystem.out.println(PrintDataMemory());
		System.out.println("After execution Register File:  \n " + PrintRegisterFile());
		System.out.println("");
		System.out.println("");
		System.out.println("After execution Data Memory:  \n " + PrintDataMemory());

//		System.out.println(intToTwoComplement(300,9));
//		System.out.println(twoComplementToInt(getSubstring(intToTwoComplement(300,9),0,9)));
//		
		
//		for (int i=0 ;i<instructionMemory.length;i++) {
//		  System.out.println(instructionMemory[i]);
//		
//	}	

		//System.out.print(twoComplementToInt("001010"));
}
}
//MOVI R0 20
//MOVI R9 10
//MOVI R2 4
//MOVI R3 -3
//ADD R2 R3 
//SUB R1 R2 
//MUL R0 R9 
//AND R0 R2
//MOVI R10 12
//BR R9 R10
//EOR R0 R1 
//MOVI R4 0 
//MOVI R0 0 
//BEQZ R0 2 
//MOVI R4 1 
//SAL R1 2 
//SAR R2 1
//STR R0 0 
//STR R1 1 
//LDR R5 0 
//LDR R6 1 
