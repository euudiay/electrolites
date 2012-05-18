PRY=electrolites
SRC=$(PRY).tex
PDF=$(PRY).pdf

.PHONY: show

all:
	pdflatex $(SRC)
	
show: all
	evince $(PDF)
	
clean:
	rm -f *.log *.aux *.toc *.lof *.lot *.out
