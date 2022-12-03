import Data.Char (ord, isLower, isUpper)
import Data.List.Split
import Data.Maybe
import qualified Data.Set as Set

main = do
    content <- readFile "input.txt"
    print $ sum . map (priority . findDuplicate) . lines $ content
    print $ sum . map (priority . findBadge) . (chunksOf 3) . lines $ content

priority :: Maybe Char -> Int
priority Nothing = 0
priority (Just c)
    | isLower c = ord c - ord 'a' + 1
    | isUpper c = ord c - ord 'A' + 27
    | otherwise = 0

findDuplicate :: Ord a => [a] -> Maybe a
findDuplicate lst = listToMaybe . Set.toList $ foldr1 Set.intersection $ map Set.fromList $ halves lst where
    halves l = chunksOf (div (length l) 2) l

findBadge :: Ord a => [[a]] -> Maybe a
findBadge lines = listToMaybe . Set.toList $ foldr1 Set.intersection $ (map Set.fromList) lines
