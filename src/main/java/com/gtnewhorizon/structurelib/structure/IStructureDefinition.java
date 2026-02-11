package com.gtnewhorizon.structurelib.structure;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Arrays;

import static com.gtnewhorizon.structurelib.StructureLib.DEBUG_MODE;

public interface IStructureDefinition<T> {
	/**
	 * Used internally
	 *
	 * Might, but is not required, to throw {@link java.util.NoSuchElementException} if the given structure piece is not found.
	 *
	 * @param name same name as for other methods here
	 * @return the array of elements to process
	 * @throws java.util.NoSuchElementException if the given structure piece is not found and the moon phase perfectly matches
	 */
	IStructureElement<T>[] getStructureFor(String name);

	default boolean check(T object, String piece, World world, ExtendedFacing extendedFacing,
						  int basePositionX, int basePositionY, int basePositionZ,
						  int basePositionA, int basePositionB, int basePositionC,
						  boolean forceCheckAllBlocks) {
		return this.check(object, piece, world, extendedFacing,
						  basePositionX, basePositionY, basePositionZ,
						  basePositionA, basePositionB, basePositionC,
						  forceCheckAllBlocks, this::noOpOnScan);
	}

	default boolean check(T object, String piece, World world, ExtendedFacing extendedFacing,
						  int basePositionX, int basePositionY, int basePositionZ,
						  int basePositionA, int basePositionB, int basePositionC,
						  boolean forceCheckAllBlocks, 
						  @NotNull Callback<T> callBack) {
		return iterate(object, null, getStructureFor(piece), world, extendedFacing,
					   basePositionX, basePositionY, basePositionZ,
					   basePositionA, basePositionB, basePositionC,
					   false, forceCheckAllBlocks,
					   callBack);
	}

	default boolean hints(T object, ItemStack trigger, String piece, World world, ExtendedFacing extendedFacing,
						  int basePositionX, int basePositionY, int basePositionZ,
						  int basePositionA, int basePositionB, int basePositionC) {
		return this.hints(object, trigger, piece, world, extendedFacing,
						  basePositionX, basePositionY, basePositionZ,
						  basePositionA, basePositionB, basePositionC,
						  this::noOpOnScan);
	}

	default boolean hints(T object, ItemStack trigger, String piece, World world, ExtendedFacing extendedFacing,
						  int basePositionX, int basePositionY, int basePositionZ,
						  int basePositionA, int basePositionB, int basePositionC,
						  @NotNull Callback<T> callBack) {
		return iterate(object, trigger, getStructureFor(piece), world, extendedFacing, 
					   basePositionX, basePositionY, basePositionZ, 
					   basePositionA, basePositionB, basePositionC, 
					   true, null, 
					   callBack);
	}

	default boolean build(T object, ItemStack trigger, String piece, World world, ExtendedFacing extendedFacing,
						  int basePositionX, int basePositionY, int basePositionZ,
						  int basePositionA, int basePositionB, int basePositionC) {
		return this.build(object, trigger, piece, world, extendedFacing,
						  basePositionX, basePositionY, basePositionZ,
						  basePositionA, basePositionB, basePositionC,
						  this::noOpOnScan);
	}

	default boolean build(T object, ItemStack trigger, String piece, World world, ExtendedFacing extendedFacing,
						  int basePositionX, int basePositionY, int basePositionZ,
						  int basePositionA, int basePositionB, int basePositionC,
						  @NotNull Callback<T> callBack) {
		return iterate(object, trigger, getStructureFor(piece), world, extendedFacing, 
					   basePositionX, basePositionY, basePositionZ, 
					   basePositionA, basePositionB, basePositionC, 
					   false, null, 
					   callBack);
	}

	default boolean buildOrHints(T object, ItemStack trigger, String piece, World world, ExtendedFacing extendedFacing,
								 int basePositionX, int basePositionY, int basePositionZ,
								 int basePositionA, int basePositionB, int basePositionC,
								 boolean hintsOnly) {
		return this.buildOrHints(object, trigger, piece, world, extendedFacing,
								 basePositionX, basePositionY, basePositionZ,
								 basePositionA, basePositionB, basePositionC,
								 hintsOnly, this::noOpOnScan);
	}
	
	default boolean buildOrHints(T object, ItemStack trigger, String piece, World world, ExtendedFacing extendedFacing,
								 int basePositionX, int basePositionY, int basePositionZ,
								 int basePositionA, int basePositionB, int basePositionC,
								 boolean hintsOnly,
								 @NotNull Callback<T> callBack) {
		return iterate(object, trigger, getStructureFor(piece), world, extendedFacing, 
					   basePositionX, basePositionY, basePositionZ, 
					   basePositionA, basePositionB, basePositionC, 
					   hintsOnly, null,
					   callBack);
	}

	static <T> boolean iterate(T object, ItemStack trigger, IStructureElement<T>[] elements, World world, ExtendedFacing extendedFacing,
							   int basePositionX, int basePositionY, int basePositionZ,
							   int basePositionA, int basePositionB, int basePositionC,
							   boolean hintsOnly, Boolean checkBlocksIfNotNullForceCheckAllIfTrue, 
							   @NotNull Callback<T> callBack) {
		if (world.isRemote ^ hintsOnly) {
			return false;
		}

		//change base position to base offset
		basePositionA = -basePositionA;
		basePositionB = -basePositionB;
		basePositionC = -basePositionC;

		int[] abc = new int[]{basePositionA, basePositionB, basePositionC};
		int[] xyz = new int[3];

		if (checkBlocksIfNotNullForceCheckAllIfTrue != null) {
			if (checkBlocksIfNotNullForceCheckAllIfTrue) {
				for (IStructureElement<T> element : elements) {
					if (element.isNavigating()) {
						abc[0] = (element.resetA() ? basePositionA : abc[0]) + element.getStepA();
						abc[1] = (element.resetB() ? basePositionB : abc[1]) + element.getStepB();
						abc[2] = (element.resetC() ? basePositionC : abc[2]) + element.getStepC();
					} else {
						extendedFacing.getWorldOffset(abc, xyz);
						xyz[0] += basePositionX;
						xyz[1] += basePositionY;
						xyz[2] += basePositionZ;

						if (world.blockExists(xyz[0], xyz[1], xyz[2])) {
							val scanSuccessful = element.check(object, world, xyz[0], xyz[1], xyz[2]);

							if (DEBUG_MODE && !scanSuccessful) {
								StructureLib.LOGGER.info("Multi [{}, {}, {}] failed @ {} {}",
														 basePositionX, basePositionY, basePositionZ,
														 Arrays.toString(xyz),
														 Arrays.toString(abc));
							}

							val keepScanning = callBack.onElementScan(xyz[0], xyz[1], xyz[2],
																	  abc[0], abc[1], abc[2],
																	  world, object, element, scanSuccessful);

							if (!keepScanning) {
								return false;
							}
						} else {
							val keepScanning = callBack.onElementScan(xyz[0], xyz[1], xyz[2],
																	  abc[0], abc[1], abc[2],
																	  world, object, element, false);
							if (DEBUG_MODE) {
								StructureLib.LOGGER.info("Multi [{}, {}, {}] !blockExists @ {} {}",
														 basePositionX, basePositionY, basePositionZ,
														 Arrays.toString(xyz),
														 Arrays.toString(abc));

							}

							if (!keepScanning) {
								return false;
							}
						}
						abc[0] += 1;
					}
				}
			} else {
				for (IStructureElement<T> element : elements) {
					if (element.isNavigating()) {
						abc[0] = (element.resetA() ? basePositionA : abc[0]) + element.getStepA();
						abc[1] = (element.resetB() ? basePositionB : abc[1]) + element.getStepB();
						abc[2] = (element.resetC() ? basePositionC : abc[2]) + element.getStepC();
					} else {
						extendedFacing.getWorldOffset(abc, xyz);
						xyz[0] += basePositionX;
						xyz[1] += basePositionY;
						xyz[2] += basePositionZ;

						if (world.blockExists(xyz[0], xyz[1], xyz[2])) {
							val scanSuccessful = element.check(object, world, xyz[0], xyz[1], xyz[2]);

							if (DEBUG_MODE && !scanSuccessful) {
								StructureLib.LOGGER.info("Multi [{}, {}, {}] failed @ {} {}",
														 basePositionX, basePositionY, basePositionZ,
														 Arrays.toString(xyz),
														 Arrays.toString(abc));
							}

							val keepScanning = callBack.onElementScan(xyz[0], xyz[1], xyz[2],
																	  abc[0], abc[1], abc[2],
																	  world, object, element, scanSuccessful);

							if (!keepScanning) {
								return false;
							}
						} else {
							//structure was previous already good but here we saw a unloaded chunk don't fail it here
							callBack.onElementScan(xyz[0], xyz[1], xyz[2],
												   abc[0], abc[1], abc[2],
												   world, object, element, false);
							if (DEBUG_MODE) {
								StructureLib.LOGGER.info("Multi [{}, {}, {}] !blockExists @ {} {}",
														 basePositionX, basePositionY, basePositionZ,
														 Arrays.toString(xyz),
														 Arrays.toString(abc));

							}
						}
						abc[0] += 1;
					}
				}
			}
		} else {
			if (hintsOnly) {
				for (IStructureElement<T> element : elements) {
					if (element.isNavigating()) {
						abc[0] = (element.resetA() ? basePositionA : abc[0]) + element.getStepA();
						abc[1] = (element.resetB() ? basePositionB : abc[1]) + element.getStepB();
						abc[2] = (element.resetC() ? basePositionC : abc[2]) + element.getStepC();
					} else {
						extendedFacing.getWorldOffset(abc, xyz);
						xyz[0] += basePositionX;
						xyz[1] += basePositionY;
						xyz[2] += basePositionZ;

						element.spawnHint(object, world, xyz[0], xyz[1], xyz[2], trigger);
                        callBack.onElementScan(xyz[0], xyz[1], xyz[2], abc[0], abc[1], abc[2], world, object, element, true);

                        abc[0] += 1;
					}
				}
			} else {
				for (IStructureElement<T> element : elements) {
					if (element.isNavigating()) {
						abc[0] = (element.resetA() ? basePositionA : abc[0]) + element.getStepA();
						abc[1] = (element.resetB() ? basePositionB : abc[1]) + element.getStepB();
						abc[2] = (element.resetC() ? basePositionC : abc[2]) + element.getStepC();
					} else {
						extendedFacing.getWorldOffset(abc, xyz);
						xyz[0] += basePositionX;
						xyz[1] += basePositionY;
						xyz[2] += basePositionZ;

						if (world.blockExists(xyz[0], xyz[1], xyz[2])) {
							element.placeBlock(object, world, xyz[0], xyz[1], xyz[2], trigger);
						}

						callBack.onElementScan(xyz[0], xyz[1], xyz[2], abc[0], abc[1], abc[2], world, object, element, true);
						abc[0] += 1;
					}
				}
			}
		}
		return true;
	}

	static <T> StructureDefinition.Builder<T> builder() {
		return StructureDefinition.builder();
	}

	@FunctionalInterface
	interface Callback<T> {
		/**
		 * A callback that gets invoked after each element scan
		 * @param x
		 * @param y
		 * @param z
		 * @param a
		 * @param b
		 * @param c
		 * @param world
		 * @param object
		 * @param element
		 * @param scanSuccessful
		 * @return true to continue scanning the structure, false to stop
		 */
		boolean onElementScan(int x, int y, int z, int a, int b, int c, World world, T object, IStructureElement<T> element, boolean scanSuccessful);
	}

	default boolean noOpOnScan(int x, int y, int z, int a, int b, int c, World world, T object, IStructureElement<T> element, boolean scanSuccessful) {
		return scanSuccessful;
	}
}
