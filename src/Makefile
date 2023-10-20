
MAC_GUROBI_HOME 	= /Library/gurobi301/mac64
MAC_GUROBIJARDIR 	= $(MAC_GUROBI_HOME)/lib
MAC_CPLEXDIR 		= /Users/bpaynter/Eclipse/cplex121/cplex121te
MAC_WHEELLUGHOLES 	= /Users/bpaynter/Eclipse/WheelLugHoles
LINUX_GUROBI_HOME 	= /home/bpaynter/eclipse/gurobi301/linux32
LINUX_CPLEXDIR 		= /home/bpaynter/eclipse/cplex/cplex121te
LINUX_WHEELLUGHOLES 	= /home/bpaynter/eclipse/WheelLugHoles
LINUX_GUROBIJARDIR 	= $(LINUX_GUROBI_HOME)/lib


JAVAC = javac

MAC_JOPT  = -classpath $(MAC_CPLEXDIR)/lib/cplex.jar:$(MAC_GUROBIJARDIR)/gurobi.jar
LINUX_JOPT  = -classpath $(LINUX_CPLEXDIR)/lib/cplex.jar:$(LINUX_GUROBIJARDIR)/gurobi.jar

# ---------------------------------------------------------------------
# Link options and libraries
# ---------------------------------------------------------------------

JAVA      = java -d64 $(JOPT)


allmac:
	$(JAVAC) $(MAC_JCFLAGS) -d $(MAC_WHEELLUGHOLES)/bin $(JAVA_EX) 
alllinux:
	$(JAVAC) $(LINUX_JCFLAGS) -d $(LINUX_WHEELLUGHOLES)/bin $(JAVA_EX)

execute: all
	make execute_java

MAC_JCFLAGS = $(MAC_JOPT)
LINUX_JCFLAGS = $(LINUX_JOPT)


#------------------------------------------------------------
#  make all      : to compile the examples. 
#  make execute  : to compile and execute the examples.
#------------------------------------------------------------


JAVA_EX = exact/*.java heuristics/*.java networks/*.java \
		problem/*.java util/*.java


all_java: $(JAVA_EX)

execute_java: $(JAVA_EX)
	 $(JAVA) Goalex1 $(EXDATA)/mexample.mps
	 $(JAVA) Goalex2
	 $(JAVA) Goalex3 $(EXDATA)/mexample.mps
	 $(JAVA) LPex1 -r
	 $(JAVA) LPex2 $(EXDATA)/example.mps p
	 $(JAVA) LPex3
	 $(JAVA) LPex4
	 $(JAVA) LPex6
	 $(JAVA) LPex7 $(EXDATA)/afiro.mps p
	 $(JAVA) MIPex1
	 $(JAVA) MIPex2 $(EXDATA)/mexample.mps
	 $(JAVA) MIPex3
	 $(JAVA) MIPex4 $(EXDATA)/p0033.mps l
	 $(JAVA) MIQPex1
	 $(JAVA) QCPex1
	 $(JAVA) QPex1
	 $(JAVA) QPex2 $(EXDATA)/qpex.lp o
	 $(JAVA) Blend
	 $(JAVA) CplexServer
	 $(JAVA) CutStock
	 $(JAVA) Diet
	 $(JAVA) Etsp
	 $(JAVA) Facility
	 $(JAVA) FixCost1
	 $(JAVA) FoodManufact
	 $(JAVA) InOut1
	 $(JAVA) InOut3
	 $(JAVA) MixBlend
	 $(JAVA) Populate $(EXDATA)/location.lp
	 $(JAVA) Rates
	 $(JAVA) Steel
	 $(JAVA) Transport 1
	 $(JAVA) TuneSet $(EXDATA)/p0033.mps
	 $(JAVA) Warehouse
	 $(JAVA) AdMIPex1 $(EXDATA)/mexample.mps
	 $(JAVA) AdMIPex2 $(EXDATA)/p0033.mps
	 $(JAVA) AdMIPex3 $(EXDATA)/sosex3.lp
	 $(JAVA) AdMIPex4
	 $(JAVA) AdMIPex5
	 $(JAVA) AdMIPex6 $(EXDATA)/mexample.mps

# ------------------------------------------------------------

clean :
	/bin/rm -rf *.o *~ *.class
	/bin/rm -rf $(C_EX) $(CPP_EX)
	/bin/rm -rf *.mps *.ord *.sos *.lp *.sav *.net *.msg *.log *.clp

# ------------------------------------------------------------
#
# The examples
#

LPex1.class: $(EXSRCJAVA)/LPex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/LPex1.java 

LPex2.class: $(EXSRCJAVA)/LPex2.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/LPex2.java 

LPex3.class: $(EXSRCJAVA)/LPex3.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/LPex3.java 

LPex4.class: $(EXSRCJAVA)/LPex4.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/LPex4.java 

LPex6.class: $(EXSRCJAVA)/LPex6.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/LPex6.java 

LPex7.class: $(EXSRCJAVA)/LPex7.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/LPex7.java 

MIPex1.class: $(EXSRCJAVA)/MIPex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/MIPex1.java 

MIPex2.class: $(EXSRCJAVA)/MIPex2.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/MIPex2.java 

MIPex3.class: $(EXSRCJAVA)/MIPex3.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/MIPex3.java 

MIPex4.class: $(EXSRCJAVA)/MIPex4.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/MIPex4.java 

MIQPex1.class: $(EXSRCJAVA)/MIQPex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/MIQPex1.java 

Goalex1.class: $(EXSRCJAVA)/Goalex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Goalex1.java 

Goalex2.class: $(EXSRCJAVA)/Goalex2.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Goalex2.java 

Goalex3.class: $(EXSRCJAVA)/Goalex3.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Goalex3.java 

AdMIPex1.class: $(EXSRCJAVA)/AdMIPex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/AdMIPex1.java

AdMIPex2.class: $(EXSRCJAVA)/AdMIPex2.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/AdMIPex2.java

AdMIPex3.class: $(EXSRCJAVA)/AdMIPex3.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/AdMIPex3.java

AdMIPex4.class: $(EXSRCJAVA)/AdMIPex4.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/AdMIPex4.java

AdMIPex5.class: $(EXSRCJAVA)/AdMIPex5.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/AdMIPex5.java

AdMIPex6.class: $(EXSRCJAVA)/AdMIPex6.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/AdMIPex6.java

QCPex1.class: $(EXSRCJAVA)/QCPex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/QCPex1.java 

QPex1.class: $(EXSRCJAVA)/QPex1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/QPex1.java 

QPex2.class: $(EXSRCJAVA)/QPex2.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/QPex2.java 

Diet.class: $(EXSRCJAVA)/Diet.java $(EXSRCJAVA)/InputDataReader.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Diet.java \
	                         $(EXSRCJAVA)/InputDataReader.java 

Etsp.class: $(EXSRCJAVA)/Etsp.java $(EXSRCJAVA)/InputDataReader.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Etsp.java \
	                         $(EXSRCJAVA)/InputDataReader.java 

Blend.class: $(EXSRCJAVA)/Blend.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Blend.java

MixBlend.class: $(EXSRCJAVA)/MixBlend.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/MixBlend.java

CplexServer.class: $(EXSRCJAVA)/CplexServer.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/CplexServer.java

CutStock.class: $(EXSRCJAVA)/CutStock.java $(EXSRCJAVA)/InputDataReader.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/InputDataReader.java \
                                 $(EXSRCJAVA)/CutStock.java

Facility.class: $(EXSRCJAVA)/Facility.java $(EXSRCJAVA)/InputDataReader.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/InputDataReader.java \
                                 $(EXSRCJAVA)/Facility.java

FixCost1.class: $(EXSRCJAVA)/FixCost1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/FixCost1.java

FoodManufact.class: $(EXSRCJAVA)/FoodManufact.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/FoodManufact.java

InOut1.class: $(EXSRCJAVA)/InOut1.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/InOut1.java

InOut3.class: $(EXSRCJAVA)/InOut3.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/InOut3.java

Populate.class: $(EXSRCJAVA)/Populate.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Populate.java

TuneSet.class: $(EXSRCJAVA)/TuneSet.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/TuneSet.java

Rates.class: $(EXSRCJAVA)/Rates.java $(EXSRCJAVA)/InputDataReader.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/InputDataReader.java \
                                 $(EXSRCJAVA)/Rates.java

Steel.class: $(EXSRCJAVA)/Steel.java $(EXSRCJAVA)/InputDataReader.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/InputDataReader.java \
                                 $(EXSRCJAVA)/Steel.java

Transport.class: $(EXSRCJAVA)/Transport.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Transport.java

Warehouse.class: $(EXSRCJAVA)/Warehouse.java
	$(JAVAC) $(JCFLAGS) -d . $(EXSRCJAVA)/Warehouse.java

# Local Variables:
# mode: makefile
# End:
