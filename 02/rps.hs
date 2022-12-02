import Data.Either

main = do
       content <- readFile "input.txt"
       print . sum . (map $ fromRight 0) . (map score1) . lines $ content
       print . sum . (map $ fromRight 0) . (map score2) . lines $ content

score1 :: String -> Either String Int
score1 game = case game of "A X" -> Right (3 + 1)
                           "A Y" -> Right (6 + 2)
                           "A Z" -> Right (0 + 3)
                           "B X" -> Right (0 + 1)
                           "B Y" -> Right (3 + 2)
                           "B Z" -> Right (6 + 3)
                           "C X" -> Right (6 + 1)
                           "C Y" -> Right (0 + 2)
                           "C Z" -> Right (3 + 3)
                           otherwise -> Left $ "bad game string " ++ game

score2 :: String -> Either String Int
score2 game = case game of "A X" -> Right (0 + 3)
                           "A Y" -> Right (3 + 1)
                           "A Z" -> Right (6 + 2)
                           "B X" -> Right (0 + 1)
                           "B Y" -> Right (3 + 2)
                           "B Z" -> Right (6 + 3)
                           "C X" -> Right (0 + 2)
                           "C Y" -> Right (3 + 3)
                           "C Z" -> Right (6 + 1)
                           otherwise -> Left $ "bad game string " ++ game
