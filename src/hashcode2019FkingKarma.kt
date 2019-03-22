package hashcode2019FkingKarma

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.random.Random

var inputPath = "c_memorable_moments.txt"
var outputPath = "c_memorable_moments.out"
var coeff_a = 0.01
var coeff_b = 0.4
var mode = 3
var seed = 42L
var pop = mutableMapOf<Int,Int>()
var popmax =0.0

fun main(args: Array<String>) {
    for (i in 0 until args.size) {
        System.out.println(args[i])
        if (i == 0) inputPath = args[i]
        if (i == 1) outputPath = args[i]
        if (i == 2) coeff_a = java.lang.Double.valueOf(args[i])
        if (i == 3) coeff_b = java.lang.Double.valueOf(args[i])
        if (i == 4) mode = args[i].toInt()
    }
    var (hMap,vMap ,indexMap) = readInput(inputPath)
    hMap as MutableMap<Int,MutableList<Int>>
    vMap as MutableMap<Int,MutableList<Int>>
    var map = mutableMapOf<Int,MutableList<Int>>()
    map.putAll(hMap)
    map.putAll(vMap)

    var score = 0.0
    var list = mutableListOf<String>()
    var R = Random(seed)
    var hcandidates = hMap.keys.toMutableList()
    var vcandidates = vMap.keys.toMutableList()
    var candidates = hMap.keys.toMutableList()
    candidates.addAll(vcandidates)


    pop = computePopularity(map,candidates)
    popmax = pop.values.max()!!.toDouble()

    var rSet = mutableSetOf<Int>()

    /*
    * First Slide choice:
    *   MODE 0: Random horizontal photo
    *   MODE 1: Euristic horizontal photo
    *   MODE 2: Random vertical photos
    *   MODE 3: Euristic vertical photos
    * */
    if (mode==0){

        var r = hMap.keys.toList()[R.nextInt(hMap.keys.size)]

        storeBestH(list,r)
        rSet = if (hcandidates.contains(r))hMap[r]!!.toMutableSet()else vMap[r]!!.toMutableSet()
        if (hcandidates.contains(r))hcandidates.remove(r)else vcandidates.remove(r)
        candidates.remove(r)
    }else if (mode==1){

        var r = findFirst(hMap)
        storeBestH(list,r)

        rSet = if (hcandidates.contains(r))hMap[r]!!.toMutableSet()else vMap[r]!!.toMutableSet()
        if (hcandidates.contains(r))hcandidates.remove(r)else vcandidates.remove(r)
        candidates.remove(r)
    }else if (mode==2){
        var cand = vMap.keys.toMutableList()
        var one = cand[R.nextInt(cand.size)]
        cand.remove(one)
        var two = cand[R.nextInt(cand.size)]
        var r = mutableListOf<Int>()
        r.add(one)
        r.add(two)
        storeBestV(list,one,two)

        rSet =  vMap[one]!!.toMutableSet()
        rSet.addAll(vMap[two]!!.toMutableSet())
        vcandidates.remove(one)
        vcandidates.remove(two)
        candidates.remove(one)
        candidates.remove(two)
    }else{
        var r = findFirstTwo(vMap)
        storeBestV(list,r[0].first,r[1].first)

        rSet =  vMap[r[0].first]!!.toMutableSet()
        rSet.addAll(vMap[r[1].first]!!.toMutableSet())
        vcandidates.remove(r[0].first)
        vcandidates.remove(r[1].first)
        candidates.remove(r[0].first)
        candidates.remove(r[1].first)
    }
    var starting_number = candidates.size

    while (candidates.isNotEmpty()){

        pop = computePopularity(map,candidates)
        popmax = pop.values.max()!!.toDouble()

        var neighH = Pair<Int,Double>(-1,-1.0)
        var valH = -1.0
        if (hcandidates.isNotEmpty()){
            neighH = bestOneNeigh(hcandidates,hMap,rSet)
            valH = measureLowLevel0(rSet, hMap[neighH.first]!!.toMutableSet())
        }

        var neighV = mutableListOf<Pair<Int,Double>>()
        var valV :Double = -1.0
        if (vcandidates.isNotEmpty()){
            neighV = bestTwoNeigh(vcandidates,vMap,rSet)
            var setV = vMap[neighV[0].first]!!.toMutableSet()
            if(neighV.size>1){
                setV.addAll(vMap[neighV[1].first]!!.toMutableSet())
            }
            valV = measureLowLevel0(rSet!!, setV)
        }

        score += maxOf(valH,valV)

        if(valV>valH){
            if(neighV.size>1){
                list = storeBestV(list,neighV[0].first,neighV[1].first)
            }else{
                list = storeBestH(list,neighV[0].first)
            }

            rSet = mutableSetOf()
            neighV.forEach{

                candidates = removeBest1(it.first,candidates)
                vcandidates = removeBest1(it.first,vcandidates)
                rSet.addAll(vMap[it.first]!!.toMutableSet())

            }


        }else{
            //remove and update candidates
            list = storeBestH(list,neighH.first)
            candidates = removeBest1(neighH.first,candidates)
            hcandidates = removeBest1(neighH.first,hcandidates)
            rSet = mutableSetOf()
            rSet = hMap[neighH.first]!!.toMutableSet()
            if(neighH.first==-1)println(rSet)
        }


            println(candidates.size.toString() + "   " + score + "  " + (score/(starting_number-candidates.size)))
    }

    writeOutput(outputPath,list)

}

fun findFirst(map1 : MutableMap<Int,MutableList<Int>>):Int{
    var map = mutableMapOf<Int,MutableList<Int>>()
    map.putAll(map1)
    var pop = mutableMapOf<Int,Int>()
    map.entries.stream().forEach { photo->
        photo.value.stream().forEach { tag ->
            if (!pop.containsKey(tag))pop[tag] = 0
            pop[tag] = pop[tag]!! + 1
        }
    }
    var best = map.entries.stream().map { photo ->
        var sum = photo.value.stream().mapToInt { it ->
            pop[it]!!
        }.max().asInt.toDouble()
        Pair<Int,Double>(photo.key,sum)
    }.max(Comparator.comparingDouble { it.second })
    best.let { println(best.get().first )}
    return best.let { best.get().first }
}

fun findFirstTwo(map2 : MutableMap<Int,MutableList<Int>>):List<Pair<Int,Double>>{
    var map = mutableMapOf<Int,MutableList<Int>>()
    map.putAll(map2)
    var pop = mutableMapOf<Int,Int>()
    map2.entries.stream().forEach { photo->
        photo.value.stream().forEach { tag ->
            if (!pop.containsKey(tag))pop[tag] = 0
            pop[tag] = pop[tag]!! + 1
        }
    }
    var best = map2.entries.stream().map { photo ->
        var sum = photo.value.stream().mapToInt { it ->
            pop[it]!!
        }.max().asInt.toDouble()
        Pair<Int,Double>(photo.key,sum)
    }.max(Comparator.comparingDouble { it.second })
    var best1_ = if(best.isPresent()) best.get() else Pair<Int, Double>(-1, -1.0)
    var candidates2 = map2.keys.toMutableList()
    candidates2.remove(best1_.first)
    var best2 = candidates2.parallelStream()
        .map { item_b ->
            var set_b = map[item_b]!!.toMutableSet()
            set_b.addAll(map[best1_.first]!!.toMutableSet())
            Pair<Int,Double>(item_b,euristicLowLevel1(map2[best1_.first]!!.toMutableSet(),set_b,pop))
        }
        .filter(Objects::nonNull)
        .max(Comparator.comparingDouble { it.second })

    if (best2.isPresent()) return mutableListOf(best1_,best2.get()) else return mutableListOf(best1_)
}

fun bestOneNeigh(candidates: MutableList<Int>,map : MutableMap<Int,MutableList<Int>>, item : MutableSet<Int>): Pair<Int, Double> {
    var best = candidates.parallelStream()
        .map { item_b ->
            Pair<Int,Double>(item_b,euristicLowLevel0(item,map[item_b]!!.toMutableSet())
            )
        }
        .filter(Objects::nonNull)
        .max(Comparator.comparingDouble { it.second })
    if (best.isPresent()) return best.get() else return Pair<Int, Double>(-1,-1.0)
}

fun bestTwoNeigh(candidates: MutableList<Int>,map : MutableMap<Int,MutableList<Int>>, item : MutableSet<Int>): MutableList<Pair<Int, Double>> {
    var best = candidates.parallelStream()
        .map({ item_b ->
            Pair<Int,Double>(item_b,euristicLowLevel0(item,map[item_b]!!.toMutableSet()))
        })
        .filter(Objects::nonNull)
        .max(Comparator.comparingDouble { it.second })
    var best1_ = if(best.isPresent()) best.get() else Pair<Int, Double>(-1, -1.0)
    var candidates2 = candidates.toMutableList()
    candidates2.remove(best1_.first)
    var best2 = candidates2.parallelStream()
        .map { item_b ->
            var set_b = map[item_b]!!.toMutableSet()
            set_b.addAll(map[best1_.first]!!.toMutableSet())
            Pair<Int,Double>(item_b,euristicLowLevel0(item,set_b))
        }
        .filter(Objects::nonNull)
        .max(Comparator.comparingDouble { it.second })

        if (best2.isPresent()) return mutableListOf(best1_,best2.get()) else return mutableListOf(best1_)
}

fun euristicLowLevel0(set1:MutableSet<Int>,set2:MutableSet<Int>) : Double {
    var intersection = set1.intersect(set2)
    var amb = set1 - set2
    var bma = set2 - set1

    var pop_1 =  0.0
    if (!amb.isEmpty()){
        pop_1 = amb.stream().mapToInt { it ->

            if (pop[it]!=null) pop[it]!! else 0

        }.average().asDouble
    }

    var pop_2 =  0.0
    if (!bma.isEmpty()){
        pop_2 = bma.stream().mapToInt { it ->
            pop[it]!!
        }.average().asDouble
    }
    var min = minOf(intersection.size,amb.size,bma.size).toDouble()
    // Alternative euristics
    // coeff_a = 1.5 coeff_b = 0.4 on memorable_moments
    // var total = min - coeff_b*(pop_2/ popmax+pop_1/ popmax) + coeff_a*((intersection.size).toDouble()-bma.size)/((intersection.size).toDouble()+bma.size)
    // coeff_a = 0.01 coeff_b = 0.4 on memorable_moments
    var total = min - coeff_b*(pop_2/ popmax+pop_1/ popmax) - coeff_a*bma.size
    return total
}

fun euristicLowLevel1(set1:MutableSet<Int>,set2:MutableSet<Int>,pop:MutableMap<Int,Int>) : Double {
    var intersection = set1.intersect(set2)
    var amb = set1 - set2
    var bma = set2 - set1
    var max = bma.stream().mapToInt { it ->
        pop[it]!!
    }.max()
    var total :Double
     if(max.isPresent()){
         total =max.asInt/(1.0+intersection.size)
     }else{
         total =0.0
     }
    return total
}

fun computePopularity(map: MutableMap<Int, MutableList<Int>>, candidates: MutableList<Int>): MutableMap<Int, Int>{
    var pop = mutableMapOf<Int,Int>()
    candidates.stream().forEach { photo->
        map[photo]!!.stream().forEach { tag ->
            if (!pop.containsKey(tag))pop[tag] = 0
            pop[tag] = pop[tag]!! + 1
        }
    }
    return pop
}

fun measureLowLevel0(set1:MutableSet<Int>,set2:MutableSet<Int>) : Double {
    var intersection = set1.intersect(set2)
    var amb = set1 - set2
    var bma = set2 - set1
    return minOf(intersection.size,amb.size,bma.size).toDouble()
}

fun removeBest1(item: Int,candidates: MutableList<Int>):MutableList<Int>{

    candidates.remove(item)
    return candidates
}

fun writeOutput(path: String,list: MutableList<String>){
    File(path).bufferedWriter().use { out ->
        out.appendln(list.size.toString())
        list.asSequence().forEach { out.appendln(it) }
    }
}

fun storeBestV(list:MutableList<String>, item :Int, item2:Int)
        : MutableList<String>{
    list.add(item.toString()+" "+item2.toString())
    return list
}

fun storeBestH(list:MutableList<String>, item :Int)
        : MutableList<String>{
    list.add(item.toString())
    return list
}

fun readInput(path : String): MutableList<Any> {
    val buffer : BufferedReader = BufferedReader(FileReader(path))
    var indexMap = mutableMapOf<String,Int>()
    var hMap = mutableMapOf<Int,MutableList<Int>>()
    var vMap = mutableMapOf<Int,MutableList<Int>>()
    var counter = 0
    var index = 0

    buffer.readLine()
    buffer.useLines {
        lines -> lines.forEach {
            var pattern : List<String> = it.split(" ")
            var tags = mutableListOf<Int>()
            for(i in 2 .. pattern.size-1){
                if (!indexMap.containsKey(pattern[i])){
                    indexMap.putIfAbsent(pattern[i],index)
                    index++
                }
                tags.add(indexMap.get(pattern[i])!!)
            }
            if (pattern[0].equals("H")) hMap[counter]=tags else vMap[counter] = tags
        counter++
        }
    }
    var a = mutableListOf<Any>()
    a.add(hMap)
    a.add(vMap)
    a.add(indexMap)
    return a
}